require 'sinatra'
require 'sinatra/json'
require 'sequel'
require 'sqlite3'
require 'rack/parser'

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
  def open_rent(user, bike_id)
    raise AlreadyHaveRent.new('User already rent some bike.') if has_rent?(user)

    bike = Bike[bike_id]
    gate_number = bike.gate_number
    bike.update(gate_number: -1)
    rent = Rent.create(user_id: user.id, bike_id: bike.id, openned_at: DateTime.now)
    
    {gate_number: gate_number, openned_at: rent.openned_at}
  end

  def close_rent(user, gate_number)
    rent = Rent.where(closed: false, user_id: user.id).first
    raise HaveNotRent.new('User have not rent to close.') if rent.nil?
    begin
      Bike[rent.bike_id].update(gate_number: gate_number)
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
    bikes = Bike.select(:id).where('gate_number != -1')
  rescue NotAuthorized => e
    return json msg: e.message, status: 401
  end
  
  json data: BikePresenter.wrap!(bikes), status: 200
end 

get '/has_rent' do
  puts "PARAMS: #{params}"
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.has_rent?(user)
  rescue NotAuthorized => e
    return json msg: e.message, status: 401
  end

  json data: res, status: 200
end

post '/start_rent' do
  puts "PARAMS: #{params}"
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.open_rent(user, params[:bike_id])
  rescue AlreadyHaveRent => e
    return json msg: e.message, status: 403
  rescue NotAuthorized => e
    return json msg: e.message, status: 401
  end

  json data: res, status: 202
end

post '/close_rent' do
  puts "PARAMS: #{params}" 
  use_case = RentService.new

  begin
    user = UserAuthenticator.new.authenticate(params[:login], params[:PIN])
    res = use_case.close_rent(user, params[:gate_number])
  rescue NotAuthorized => e
    return json msg: e.message, status: 401
  rescue HaveNotRent => e
    return json msg: e.message, status: 403
  rescue GateNumberInUse => e
    return json msg: e.message, status: 403
  end
  
  json date: res, status: 202
end

class AlreadyHaveRent < StandardError; end
class NotAuthorized < StandardError; end
class HaveNotRent < StandardError; end
class GateNumberInUse < StandardError; end