import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;
import static spark.Spark.stop;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class Main {

    interface Messages {
        String UNPARSEABLE_AMOUNT = "Unparseable amount value";
        String NEGATIVE_AMOUNT = "Negative amount value";
        String ZERO_AMOUNT = "Zero amount value";
        String INSUFFICIENT_FUNDS = "Insufficient funds in from account";
        String INVALID_ACCOUNT_NUMBER = "Invalid account number";
        String INVALID_FROM_ACCOUNT_NUMBER = "Invalid from account number";
        String INVALID_TO_ACCOUNT_NUMBER = "Invalid to account number";
        String UNABLE_TO_PARSE_JSON = "Unable to parse json body";
        String TO_AND_FROM_ACCOUNTS_SAME = "The to and from account numbers are the same";
        String SUCCESS = "success";
    }

    static HashMap<String, Account> accounts = new HashMap<>();

    private static void end(int status, String message) {
        HashMap<String, Object> output = new HashMap<>();
        output.put("message", message);
        Gson gson = new Gson();
        throw halt(status, gson.toJson(output));
    }

    private static Account getAccount(String accountNumber, String errorMessage) {
        if (accountNumber == null || !accounts.containsKey(accountNumber)) {
            end(400, errorMessage);
        }
        return accounts.get(accountNumber);
    }

    private static HashMap<String, String> parse(String body) {
        if (body == null || body.equals("")) {
            return new HashMap<>();
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(body, HashMap.class);
        }
        catch (Exception e) {
            end(400, Messages.UNABLE_TO_PARSE_JSON);
            return null;
        }
    }

    private static void defineRoutes() {
        get("/accounts/:number", (Request req, Response res) -> {
            String accountNumber = req.params(":number");
            Account account = getAccount(accountNumber, Messages.INVALID_ACCOUNT_NUMBER);

            try {
                account.lock();

                HashMap<String, Object> output = new HashMap<>();
                output.put("message", Messages.SUCCESS);
                output.put("balance", "" + account.getBalance());
                Gson gson = new Gson();
                return gson.toJson(output);
            }
            finally {
                account.unlock();
            }
        });

        get("/accounts", (Request req, Response res) -> {
            HashMap<String, Object> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            HashMap<String, String> balances = new HashMap<>();

            for (Account account : accounts.values()) {
                balances.put(account.getAccountNumber(), "" + account.getBalance());
            }

            output.put("accounts", balances);
            Gson gson = new Gson();
            return gson.toJson(output);
        });

        post("/accounts", (Request req, Response res) -> {
            Map<String, String> params = parse(req.body());
            double initialBalance = 0;

            // Validation
            {
                if (params.containsKey("initialBalance")) {
                    try {
                        initialBalance = Double.parseDouble(params.get("initialBalance"));
                        if (Double.compare(initialBalance, 0) < 0) {
                            end(400, Messages.NEGATIVE_AMOUNT);
                        }
                    } catch (NumberFormatException | ClassCastException | NullPointerException e) {
                        end(400, Messages.UNPARSEABLE_AMOUNT);
                    }
                }
            }

            Account account = new Account();

            if (params.containsKey("initialBalance")) {
                account.adjustBalance(initialBalance);
            }

            accounts.put(account.getAccountNumber(), account);

            HashMap<String, Object> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            output.put("account", account.getAccountNumber());
            Gson gson = new Gson();
            return gson.toJson(output);
        });

        delete("/accounts/:account", (Request req, Response res) -> {
            String accountNumber = req.params(":account");
            Account account = getAccount(accountNumber, Messages.INVALID_ACCOUNT_NUMBER);

            try {
                account.lock();

                accounts.remove(accountNumber);

                HashMap<String, Object> output = new HashMap<>();
                output.put("message", Messages.SUCCESS);
                Gson gson = new Gson();
                return gson.toJson(output);
            }
            finally {
                account.unlock();
            }
        });

        put("/accounts/:account", (Request req, Response res) -> {
            Map<String, String> params = parse(req.body());
            String accountNumber = req.params(":account");
            Account account = getAccount(accountNumber, Messages.INVALID_ACCOUNT_NUMBER);

            try {
                account.lock();

                double amt = 0;

                // Validation.
                {
                    try {
                        amt = Double.parseDouble(params.get("balance"));
                        if (Double.compare(amt, 0) < 0) {
                            end(400, Messages.NEGATIVE_AMOUNT);
                        }
                    } catch (NumberFormatException | ClassCastException | NullPointerException e) {
                        end(400, Messages.UNPARSEABLE_AMOUNT);
                    }
                }

                account.setBalance(amt);

                HashMap<String, String> output = new HashMap<>();
                output.put("message", Messages.SUCCESS);
                Gson gson = new Gson();
                return gson.toJson(output);
            }
            finally {
                account.unlock();
            }
        });

        post("/transfer", (Request req, Response res) -> {
            Map<String, String> params = parse(req.body());
            String from = params.get("from");
            String to = params.get("to");

            if (to.equals(from)) {
                end(400, Messages.TO_AND_FROM_ACCOUNTS_SAME);
            }

            double amt = 0;

            Account fromAccount = getAccount(from, Messages.INVALID_FROM_ACCOUNT_NUMBER);
            Account toAccount = getAccount(to, Messages.INVALID_TO_ACCOUNT_NUMBER);

            try {
                // Acquire locks for accounts in alphabetic order to avoid deadlock.
                if (from.compareTo(to) < 0) {
                    fromAccount.lock();
                    toAccount.lock();
                }
                else {
                    toAccount.lock();
                    fromAccount.lock();
                }

                // Validation.
                {
                    try {
                        amt = Double.parseDouble(params.get("amount"));
                        if (Double.compare(amt, 0) < 0) {
                            end(400, Messages.NEGATIVE_AMOUNT);
                        }
                        if (Double.compare(amt, 0) == 0) {
                            end(400, Messages.ZERO_AMOUNT);
                        }
                        if (Double.compare(amt, fromAccount.getBalance()) > 0) {
                            end(400, Messages.INSUFFICIENT_FUNDS);
                        }
                    } catch (NumberFormatException | ClassCastException | NullPointerException e) {
                        end(400, Messages.UNPARSEABLE_AMOUNT);
                    }
                }

                fromAccount.adjustBalance(-amt);
                toAccount.adjustBalance(amt);

                HashMap<String, Object> output = new HashMap<>();
                output.put("message", Messages.SUCCESS);
                output.put("fromBalance", "" + fromAccount.getBalance());
                output.put("toBalance", "" + toAccount.getBalance());
                Gson gson = new Gson();
                return gson.toJson(output);
            }
            finally {
                toAccount.unlock();
                fromAccount.unlock();
            }
        });
    }

    static void printUsage() {
        System.out.println("Usage: server [port_number]");
    }

    public static void start(int port) {
        port(port);
        defineRoutes();
    }

    public static void kill() {
        stop();
    }

    public static void await() {
        awaitInitialization();
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e) {
                System.err.println("Invalid port number given");
                printUsage();
                return;
            }
        }

        start(port);
    }
}
