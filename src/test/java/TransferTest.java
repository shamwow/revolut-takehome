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
public class TransferTest extends BaseTest {
    @Before
    public void createAccounts() throws Exception {
        accounts.add(Helpers.createAccount("100"));
        accounts.add(Helpers.createAccount("200"));
        accounts.add(Helpers.createAccount("300"));
    }

    @Test
    public void properlyTransfer() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", "100")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        Helpers.assertBalance(accounts.get(0), 0);
        Helpers.assertBalance(accounts.get(1), 300);
        Helpers.assertBalance(accounts.get(2), 300);
    }

    @Test
    public void doesNotTransferNegativeAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", "-100")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.NEGATIVE_AMOUNT, json.get("message"));
    }

    @Test
    public void doesNotTransferZeroAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", "0")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.ZERO_AMOUNT, json.get("message"));
    }

    @Test
    public void doesNotTransferWhenInsufficientFunds() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(2))
                        .put("to", accounts.get(1))
                        .put("amount", "400")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INSUFFICIENT_FUNDS, json.get("message"));
    }

    @Test
    public void doesNotTransferToSameAccount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(2))
                        .put("to", accounts.get(2))
                        .put("amount", "150")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.TO_AND_FROM_ACCOUNTS_SAME, json.get("message"));
    }

    @Test
    public void invalidFrom() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", "INVALID")
                        .put("to", accounts.get(1))
                        .put("amount", "150")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INVALID_FROM_ACCOUNT_NUMBER, json.getString("message"));
    }

    @Test
    public void invalidTo() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("to", "INVALID")
                        .put("from", accounts.get(1))
                        .put("amount", "150")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INVALID_TO_ACCOUNT_NUMBER, json.getString("message"));
    }

    @Test
    public void unparseableAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", "INVALID")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNPARSEABLE_AMOUNT, json.getString("message"));
    }

    @Test
    public void unparseableDoubleAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", 100)
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNPARSEABLE_AMOUNT, json.getString("message"));
    }

    @Test
    public void invalidJson() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body("INVALID")
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.UNABLE_TO_PARSE_JSON, json.getString("message"));
    }
}
