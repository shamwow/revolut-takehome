import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.HashMap;

import static spark.Spark.*;
import static spark.Spark.stop;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class Main {

    interface Messages {
        String UNPARSEABLE_AMOUNT = "Unparseable amount value";
        String INVALID_ACCOUNT_NUMBER = "Invalid account number";
        String INVALID_FROM_ACCOUNT_NUMBER = "Invalid from account number";
        String INVALID_TO_ACCOUNT_NUMBER = "Invalid to account number";
        String UNABLE_TO_PARSE_JSON = "Unable to parse json body";
        String SUCCESS = "success";
    }

    static HashMap<String, Account> accounts = new HashMap<>();

    private static void end(int status, String message) {
        HashMap<String, Object> output = new HashMap<>();
        output.put("message", message);
        Gson gson = new Gson();
        halt(status, gson.toJson(output));
    }

    private static Map<String, String> parse(String body) {
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
            String account = req.params(":number");

            // Validation.
            {
                if (account == null || !accounts.containsKey(account)) {
                    end(400, Messages.INVALID_ACCOUNT_NUMBER);
                }
            }

            HashMap<String, Object> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            output.put("balance", "" + accounts.get(account).getBalance());
            Gson gson = new Gson();
            return gson.toJson(output);
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
                    } catch (NumberFormatException | ClassCastException e) {
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
            String account = req.params().get(":account");
            // Validation.
            {
                if (account == null || !accounts.containsKey(account)) {
                    end(400, Messages.INVALID_ACCOUNT_NUMBER);
                }
            }

            accounts.remove(account);

            HashMap<String, Object> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            Gson gson = new Gson();
            return gson.toJson(output);
        });

        put("/accounts/:account", (Request req, Response res) -> {
            Map<String, String> params = parse(req.body());
            String account = req.params().get(":account");

            double amt = 0;

            // Validation.
            {
                if (account == null || !accounts.containsKey(account)) {
                    end(400, Messages.INVALID_ACCOUNT_NUMBER);
                }
                try {
                    amt = Double.parseDouble(params.get("balance"));
                }
                catch (NumberFormatException | ClassCastException e) {
                    end(400, Messages.UNPARSEABLE_AMOUNT);
                }
            }

            accounts.get(account).setBalance(amt);

            HashMap<String, String> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            Gson gson = new Gson();
            return gson.toJson(output);
        });

        post("/transfer", (Request req, Response res) -> {
            Map<String, String> params = parse(req.body());
            String from = params.get("from");
            String to = params.get("to");
            double amt = 0;

            // Validation.
            {
                if (from == null || !accounts.containsKey(from)) {
                    end(400, Messages.INVALID_FROM_ACCOUNT_NUMBER);
                }
                if (to == null || !accounts.containsKey(to)) {
                    end(400, Messages.INVALID_TO_ACCOUNT_NUMBER);
                }
                try {
                    amt = Double.parseDouble(params.get("amount"));
                }
                catch (NumberFormatException | ClassCastException e) {
                    end(400, Messages.UNPARSEABLE_AMOUNT);
                }
            }

            accounts.get(from).adjustBalance(-amt);
            accounts.get(to).adjustBalance(amt);

            HashMap<String, Object> output = new HashMap<>();
            output.put("message", Messages.SUCCESS);
            output.put("fromBalance", "" + accounts.get(from).getBalance());
            output.put("toBalance", "" + accounts.get(to).getBalance());
            Gson gson = new Gson();
            return gson.toJson(output);
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
