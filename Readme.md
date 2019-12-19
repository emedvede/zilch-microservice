# ZILCH CARD MICROSERVICE

A microservice which accepts incoming Purchases against Zilch Cards and manages Transactions.


## Description
A Rest API to access Zilch Card accounts with the current balance of a user.
Purchase can be executed against Zilch Card what will create 4 Transactions.
The amount in Purchase will be equally distributed against all 4 transactions.
Each Transaction will be saved within a week difference Due Date starting from the first which has a due date of Purchase request. 

The balance of Zilch Card can be modified by registering transactions on the account, either debit transactions (removing funds) 
or credit transactions (adding funds).

A Purchase will only succeed if there are sufficient funds on Zilch Card for the first transaction 
(balance - Purchase_amount/4 >= 0). 

Unique global transaction id must be provided when registering a transaction on an account. 

It is also possible get current balance of a Zilch Card and get transactions.

## Api requirements and running instructions
1. Java 8
2. Maven 3 to build the application.
This will activate Flyway to run db creation scripts. 
3. Download and install PostgreSQL server (version 9.5 or higher).
It is available here:
https://www.postgresql.org/download/

Client can be installed from https://www.pgadmin.org/download/ for example

4. Connect to the Postgres server and create database -  zilch.
5. For the database zilch create schema  - schema_zilch, using SQL:
```
CREATE SCHEMA schema_zilch AUTHORIZATION postgres;
 ```
The user, schema and database can be different, but then it needs to be configured in the
``` 
zilch-microservice/src/main/resources/application.properties
```
See
```
spring.datasource.url = jdbc:postgresql://localhost:5432/zilch
spring.datasource.username = postgres
spring.datasource.password = postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.default_schema = schema_zilch
```
6. From the root folder of the application run:
``` 
mvn clean install
``` 
7. Start application by:
``` 
mvn spring-boot:run
``` 
All tables, reference data and some dummy data will be created and inserted by Flyway.

Or instead you can run the jar file and run scripts manually against schema_card:
``` 
java -jar target/zilch-microservice-1.0-SNAPSHOT.jar
``` 
8. To check that application started successfully go to:
``` 
http://localhost:8080/test
``` 
This should produce result:
``` 
Hello from Zilch microservice!
``` 
The port can be also configured from 
``` 
zilch-microservice/src/main/resources/application.properties
```
see 'server.port' property

## Api endpoints
Examples of all requests can be found in:
``` 
zilch-microservice/src/main/resources/examples/card.postman_collection.json
``` 

Http GET endpoints:
1. http://localhost:8080/zilch/cards
Gets all cards
Some cards are generated after the first start of the application by Flyway.

2. http://localhost:8080/zilch/cards/{id}
Gets card with id={id}

3. http://localhost:8080/zilch/cards/user?userId={user}
Gets list of cards by user

4. http://localhost:8080/zilch/cards/{id}/transactions
Gets list of transactions by card id
Some transactions are generated after the first start of the application by Flyway.

Http POST endpoints:
1. http://localhost:8080/zilch/cards
With the following JSON in the body:
``` 
{
"currency":"{currency}",
"userId":"{userId}"
}
``` 
Creates new card.
e.g.
``` 
{
"currency":"EUR",
"userId":"new-user"
}
``` 
Will create new card with currency EUR and userId=new-user.
The currency id should be present in the reference table 'currency'.

2. http://localhost:8080/zilch/transactions
With the following JSON in the body:
``` 
{"globalId":"557",
"currency":"EUR",
"cardId": "2",
"amount":"20",
"transactionTypeId":"D",
"description":"add money"
}
``` 
for credit transaction.
``` 
will create credit transaction.
{"globalId":"558",
"currency":"EUR",
"cardId": "2",
"amount":"20",
"transactionTypeId":"D",
"description":"add money"
}
``` 
for debit transaction.
Creates transaction.
'globalId' must be unique.
'currency','transactionTypeId' and 'cardId' must be present in the db.
'currency' should be the same as in card.

## Technology used

- PostgreSQL database, which has good concurrency support, also has ACID compliance and can be replicated.
- Spring Boot, including Spring Data JPA for JPA based repositories.
- Undertow, a web server to run the application.
- Flyway,tool db migration
- logback + slf4j for logging.
- Gson from com.google.code.gson to serialize objects to JSON.

## Support of the aspects:

1. Transactions on service and repository level ensure atomicity.
2. Identifiers for entities and primary keys in the db ensure idempotency. 
As well as unique globalIds for card transactions.
3. Scalability: This can be solved by installing card microservice application on several hosts, 
with Postgres server running on it own host/hosts(can be distributed).
Load balancer (e.g. NGINX) will share requests between the application instances and provide high-availability.
Shared cache can be configured for Spring application. For example,
Spring supports Reddis, which can be used as shared cache. 
But since this application is targeted on transactions creation, it might not be useful,
because we have to update cache too often.
4. Concurrency:
Postgres has good concurrency support.
Undertow threads count can be configured in the application.properties.
``` 
server.undertow.worker-threads
server.undertow.io-threads
``` 
This numbers should be configured based on the properties of particular host, where the application runs.
In the application there are no shared objects, so there should not be concurrency issues.

## Features not implemented
1. Security (Information Exchange)

Can be implemented using JWT.
2. Authentication
 
Can be put on the NGINX level,if used.
Or using Spring Security.
3. Authorization

Can be implemented using JWT.




 








