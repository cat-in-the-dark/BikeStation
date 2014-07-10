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
    String :pin
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
end

class Rent < Sequel::Model(:rents)
end

class RentService
  def open_rent(user, bike_id)
    bike = Bike[bike_id]
    gate_number = bike.gate_number
    bike.update(gate_number: -1)
    rent = Rent.create(user_id: user.id, bike_id: bike.id, openned_at: DateTime.now)
    
    {gate_number: gate_number, openned_at: rent.openned_at}
  end

  def close(user, gate_number)
    rent = Rent.where(closed: false, user_id: user.id)
    Bike[rent.bike_id].update(gate_number: gate_number)
    rent.update(closed: true, closed_at: DateTime.now)

    {closed_at: rent.closed_at, money: 100}
  end

  def has_rent?(user)
    !Bike.where(closed: false, user_id: user.id).empty?
  end
end

class UserAuthenticator
  def authenticate(email, pin)
    User.where(email: email, pin: pin).first
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
  bikes = Bike.select(:id).where('gate_number != -1')
  json data: BikePresenter.wrap!(bikes)
end 

get '/has_rent' do
  user = UserAuthenticator.new.authenticate(params[:email], params[:pin])
  if user.nil?
    json msg: 'Email or pin incorrect', status: 401
  else
    use_case = RentService.new

    json data: use_case.has_rent?(user)
  end
end

post '/start_rent' do
  user = UserAuthenticator.new.authenticate(params[:email], params[:pin])
  use_case = RentService.new
  res = use_case.open_rent(user, params[:bike_id])
  {data: {gate_number: res.gate_number, openned_at: res.openned_at}}
end

post '/close_rent' do 
  user = UserAuthenticator.new.authenticate(params[:email], params[:pin])
  use_case = RentService.new
  res = use_case.close_rent(user, params[:gate_number])
  {date: {closed_at: res.closed_at, money: res.money}}
end