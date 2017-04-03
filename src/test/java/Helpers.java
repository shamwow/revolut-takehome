import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import static org.junit.Assert.assertEquals;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class Helpers {

    public static String getAccountBalance(String account) throws Exception {
        HttpResponse<JsonNode> res = Unirest.get("http://localhost:8080/accounts/" + account).asJson();

        assertEquals(200, res.getStatus());

        return res.getBody().getObject().getString("balance");
    }

    public static boolean accountExists(String account) throws Exception {
        HttpResponse<JsonNode> res = Unirest.get("http://localhost:8080/accounts").asJson();

        assertEquals(200, res.getStatus());

        return res.getBody().getObject().getJSONObject("accounts").has(account);
    }

    public static String createAccount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/accounts")
                .header("accept", "application/json")
                .asJson();

        assertEquals(200, res.getStatus());
        return res.getBody().getObject().getString("account");
    }

    public static String createAccount(String initialBalance) throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/accounts")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("initialBalance", initialBalance)
                )
                .asJson();

        assertEquals(200, res.getStatus());
        return res.getBody().getObject().getString("account");
    }

    public static void assertBalance(String account, double balance) throws Exception {
        double amount = Double.parseDouble(getAccountBalance(account));
        assertEquals(0, Double.compare(amount, balance));
    }

    public static void assertDoubleEquals(String double1, String double2) throws Exception {
        double amount1 = Double.parseDouble(double1);
        double amount2 = Double.parseDouble(double2);
        assertEquals(0, Double.compare(amount1, amount2));
    }
}
