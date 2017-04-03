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
        accounts.add(Helpers.createAccount("-200"));
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
        Helpers.assertBalance(accounts.get(2), -200);
    }

    @Test
    public void properlyTransferNegativeBalance() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(0))
                        .put("to", accounts.get(1))
                        .put("amount", "-100")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        Helpers.assertDoubleEquals("200", json.getString("fromBalance"));
        Helpers.assertDoubleEquals("100", json.getString("toBalance"));
        Helpers.assertBalance(accounts.get(0), 200);
        Helpers.assertBalance(accounts.get(1), 100);
        Helpers.assertBalance(accounts.get(2), -200);
    }

    @Test
    public void properlyTransferFromNegativeBalance() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(2))
                        .put("to", accounts.get(1))
                        .put("amount", "150")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        Helpers.assertDoubleEquals("-350", json.getString("fromBalance"));
        Helpers.assertDoubleEquals("350", json.getString("toBalance"));
        Helpers.assertBalance(accounts.get(0), 100);
        Helpers.assertBalance(accounts.get(1), 350);
        Helpers.assertBalance(accounts.get(2), -350);
    }

    @Test
    public void properlyTransferToSameAccount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/transfer")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("from", accounts.get(2))
                        .put("to", accounts.get(2))
                        .put("amount", "150")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        Helpers.assertDoubleEquals("-200", json.getString("fromBalance"));
        Helpers.assertDoubleEquals("-200", json.getString("toBalance"));
        Helpers.assertBalance(accounts.get(0), 100);
        Helpers.assertBalance(accounts.get(1), 200);
        Helpers.assertBalance(accounts.get(2), -200);
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
