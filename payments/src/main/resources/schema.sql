DROP TABLE IF EXISTS User_Account;

CREATE TABLE User_Account (
    Account_No VARCHAR(255) PRIMARY KEY,
    User_Name VARCHAR(255),
    Pay_Id VARCHAR(255),
    Account_Balance DECIMAL(19 , 2)
);