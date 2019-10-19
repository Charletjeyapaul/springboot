**Payment application**

1. Assumptions
    - Transaction can be done only for registered users, whose information will be present in database.
    - Transactions will be attempted only for Accounts/PayIds within same bank/branch (which is saved in single database). Both and Sender and Receiver will be from same branch
    - Onlu AUD transactions are considered.
    - Reversal options are not considered.
    - Every account must have a valid accountNo, but may or may not have a PayID.
    - Daily transaction limit for accounts isn't considered.
    - Single transaction can be attempted at a time. Table locking/synchronisation not implemented.

2. High level description of your solution
    - The API is designed for making transactions between 2 persons using Bank account or PayId. The application is developed using spring boot.
    - The registered user's information will be present in the database.
    - Transaction service can be initiated by a post request, where the sender and receiver information and the amount to be transferred will be sent through a JSON request.
    - Based on the information from request, checks and validations are done.
    - Amount will be debited from Sender user account in database and credited to receiver user account.
    - The response message is passed to the caller.  

3. Information on any tools, libraries, etc you have chosen and why
    - H2 library is used for in-memory database. 
        - The table and the data will be created from data.sql and schema.sql, at the time of deployment. 
        - The data will persist until the service is stopped.
        - The data can be browsed or manipulated anytime after deployment using (http://localhost:8080/h2)
        - Same database is used for testing.   
    - JPA is used for repository access
    - JUnit/Mockito is used for testing the application
     
4. How to build and execute your program
- Download the application and deploy  from maven using:     mvn spring-boot:run
- Use postman or similar for sending request to the api
    - RequestType: POST
    - url: http://localhost:8080/transfer
    - Content-Type header: application/json
    - Sample request: 

        {
        	"senderAccountNo": "12400111111111",
        	"receiverAccountInfo": "0411000111",
        	"transactionType": "PAY_ID",
        	"transactionAmount": "100"
        }
    - transactionType" can be PAY_ID or BANK_ACCOUNT
    - "receiverAccountInfo" can be payId or account no from data.sql. "senderAccountNo" can only be bank account no.
    - More senders and receivers details can be used from resource/data.sql file, which is the source for database.
    - Data can be added or deleted data.sql file for testing and same can be used in request (Note: DB data is not validate for errors)
- The result can be verified from response message or using H2 database. 
- H2 in-memory database is used for saving data.
    - At the start of the application, data will be loaded from resources/data.sql
    - Data can be viewed/verified at runtime from browser with http://localhost:8080/h2 and the connecting with default values.
    - Data will persist until service is stopped.

 


