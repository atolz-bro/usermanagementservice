To run this application from the command line

-cd into usermangementservice after cloning.

-run the command ./mvnw spring-boot:run

-the application should be started already.

-for the APIs provided and how to interact with it, visit http://localhost:8080/swagger-ui/index.html

-the h2 database can be accessed via: http://localhost:8080/h2-console/ ; 

-h2 jdbc url is: jdbc:h2:mem:testdb; username=sa; password="";

-on start of the app, the app creates a default user admin with username "admin" and password "admin123";
use this to get jwt token from /login and authententicate against other endpoints for user management(e.g from swagger)

-the project was run and tested with openjdk 23

