require 'sinatra'
require 'sequel'
require 'sqlite3'

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
  end
end

unless DB.table_exists? (:rents)
  DB.create_table :rents do
    primary_key :id
    DateTime :start
    DateTime :end
    Boolean :closed, default: false
    foreign_key :bike_id, :bikes
    foreign_key :user_id, :users
  end
end

class User < Sequel::Model(:users)
end

class Bike < Sequel::Model(:bikes)
end

class Rent < Sequel::Model(:rents)
end

get '/' do
  'Hello world'
end

get '/bikes' do
  {data: [{id: 1}]}
end 

get '/is_in_rent' do
end

post '/start_rent' do
  {data: {gate_id: 1, started_at: DateTime.now}}
end

post '/close_rent' do 
  {date: {closed_at: DateTime.now, money: 100}}
end