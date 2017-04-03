import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class AccountPutTest extends BaseTest {
    @Before
    public void createAccount() throws Exception {
        accounts.add(Helpers.createAccount());
    }

    @Test
    public void properlyAdjustPositively() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("balance", "100.23")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        Helpers.assertBalance(accounts.get(0), 100.23);
    }

    @Test
    public void properlyAdjustNegatively() throws Exception {
        System.out.println(accounts.get(0));
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("balance", "-100.23")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        Helpers.assertBalance(accounts.get(0), -100.23);
    }

    @Test
    public void invalidAccountNumber() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/INVALID_NUMBER")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("balance", "-100.23")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INVALID_ACCOUNT_NUMBER, json.getString("message"));
    }

    @Test
    public void unparsableAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("balance", "helo")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNPARSEABLE_AMOUNT, json.getString("message"));
    }

    @Test
    public void unparsableDoubleAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("balance", 100)
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNPARSEABLE_AMOUNT, json.getString("message"));
    }

    @Test
    public void unparsableJsonBody() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .body("INVALID JSON")
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNABLE_TO_PARSE_JSON, json.getString("message"));
    }
}
