## Starting the server

Runnable via the IntelliJ project or via the included jar:

`java -jar revolut-takehome.jar [port=8080]`

An optional port number can be provided. Defaults to `8080`.

## Api

Notes:
- All balances are reported as strings.
- When specifying request bodies, ensure they are valid JSON and that all values are strings.

### POST `/accounts`

Creates an account.

#### JSON body parameters
- Optional String `initialBalance`: Initial balance to start the account with.

#### Returns
```
{
    message: String, // Message related to request.
    account: String // Account number of new account.
}
```

### GET `/accounts`

Gets all accounts and their balances.

#### Returns
```
{
    message: String, // Message related to request.
    accounts: {
        String: String // Map of account numbers to balances.
    }
}
```

### GET `/accounts/:account`

Gets the balance of the account with the account number `:account`.

#### Returns
```
{
    message: String, // Message related to request.
    balance: String // Balance of the specified account.
}
```

### PUT `/accounts/:account`

Updates the balance of the specified account.

#### Returns
```
{
    message: String, // Message related to request.
}
```

### DELETE `/accounts/:account`

Deletes the specified account.

#### Returns
```
{
    message: String, // Message related to request.
}
```

### POST `/transfer`

Deletes the specified account.

#### JSON body parameters
- String `from`: Account number of account to transfer money from.
- String `to`: Account number of account to transfer money to. Can be the same as `from`.
- String `amount`: Amount of money to transfer. Can be negative.

#### Returns
```
{
    message: String, // Message related to request.
    fromBalance: String, // New balance of 'from' account.
    toBalance: String, // New balance of 'to' account.
}
```

## Testing

Some integration tests can be found in the `src/test/java` directory.

