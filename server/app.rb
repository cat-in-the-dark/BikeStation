require 'sinatra'
require 'sinatra/json'
require 'sequel'
require 'sqlite3'
require 'rack/parser'

OK = 200
ACCEPTED = 202
UNAUTHORIZED = 401
FORBIDDEN = 403
NOT_FOUND = 404


use Rack::Parser, :content_types => {
  'application/json'  => Proc.new { |body| ::MultiJson.decode body }
}

DB = Sequel.sqlite('bike_station.db')

unless DB.table_exists? (:users)
  DB.create_table :users do
    primary_key :id
    String :email
    String :PIN
  end
end
unless DB.table_exists? (:bikes)  
  DB.create_table :bikes do
    primary_key :id
    foreign_key :station_id
    integer :gate_number
  end
end
unless DB.table_exists?(:stations)
  DB.create_table :stations do
    primary_key :id
    String :name
    String :address
  end
end

unless DB.table_exists? (:rents)
  DB.create_table :rents do
    primary_key :id
    DateTime :openned_at
    DateTime :closed_at
    Boolean :closed, default: false
    foreign_key :bike_id, :bikes
    foreign_key :user_id, :users
  end
end

class User < Sequel::Model(:users)
end

class Station < Sequel::Model(:stations)
end

class Bike < Sequel::Model(:bikes)
  plugin :validation_helpers
  def validate
    super
    validates_unique [:gate_number, :station_id]
  end
end

class Rent < Sequel::Model(:rents)
end

class RentService
  def open_rent(user, bike_id, station_id)
    raise AlreadyHaveRent.new('User already rent some bike.') if has_rent?(user)

    bike = Bike.where(station_id: station_id, id: bike_id).last
    raise NotFound.new('Bike not found') if bike.nil?

    gate_number = bike.gate_number
    bike.update(gate_number: -1, station_id: nil)
    rent = Rent.create(user_id: user.id, bike_id: bike.id, openned_at: DateTime.now)
    
    {gate_number: gate_number, openned_at: rent.openned_at}
  end

  def close_rent(user, gate_number, station_id)
    rent = Rent.where(closed: false, user_id: user.id).first
    raise HaveNotRent.new('User have not rent to close.') if rent.nil?

    bike = Bike[rent.bike_id]
    raise NotFound.new('Bike not foud') if bike.nil?

    begin
      bike.update(gate_number: gate_number, station_id: station_id)
    rescue Sequel::ValidationFailed => e
      raise GateNumberInUse.new('This gate is used by another bike.')
    end
    rent.update(closed: true, closed_at: DateTime.now)

    {closed_at: rent.closed_at, money: 100}
  end

  def has_rent?(user)
    !Rent.where(closed: false, user_id: user.id).empty?
  end
end

class UserAuthenticator
  def authenticate(email, pin)
    user = User.where(email: email, pin: pin).first
    raise NotAuthorized.new('Email or pin is wrong') if user.nil?
    user
  end
end

class BikePresenter
  def initialize(bike)
    @bike = bike
  end

  def wrap!
    {id: @bike.id}
  end

  def self.wrap!(bikes)
    bikes.map { |bike| BikePresenter.new(bike).wrap! }
  end
end


get '/' do
  'Hello world'
end

get '/users' do
  json data: User.last.id
end

get '/bikes' do
  puts "PARAMS: #{params}"
  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    bikes = Bike.select(:id).where('gate_number != :gate_number AND station_id = :station_id', gate_number: -1, station_id: params[:stationId])
  rescue NotAuthorized => e
    return json msg: e.message, status: UNAUTHORIZED
  end
  
  json data: BikePresenter.wrap!(bikes), status: OK
end 

get '/has_rent' do
  puts "PARAMS: #{params}"
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.has_rent?(user)
  rescue NotAuthorized => e
    return json msg: e.message, status: UNAUTHORIZED
  end

  json data: res, status: OK
end

post '/start_rent' do
  puts "PARAMS: #{params}"
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.open_rent(user, params[:bikeId], params[:stationId])
  rescue AlreadyHaveRent => e
    return json msg: e.message, status: FORBIDDEN
  rescue NotAuthorized => e
    return json msg: e.message, status: UNAUTHORIZED
  rescue NotFound => e
    return json msg: e.message, status: NOT_FOUND
  end

  json data: res, status: ACCEPTED
end

post '/close_rent' do
  puts "PARAMS: #{params}" 
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.close_rent(user, params[:gateNumber], params[:stationId])
  rescue NotAuthorized => e
    return json msg: e.message, status: UNAUTHORIZED
  rescue HaveNotRent => e
    return json msg: e.message, status: FORBIDDEN
  rescue GateNumberInUse => e
    return json msg: e.message, status: FORBIDDEN
  rescue NotFound => e
    return json msg: e.message, status: NOT_FOUND
  end
  
  json date: res, status: ACCEPTED
end

class AlreadyHaveRent < StandardError; end
class NotAuthorized < StandardError; end
class HaveNotRent < StandardError; end
class GateNumberInUse < StandardError; end
class NotFound < StandardError; end