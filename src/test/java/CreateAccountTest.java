import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class CreateAccountTest extends BaseTest {

    @Test
    public void createsAccount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/accounts")
                .header("accept", "application/json")
                .asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertNotNull(json.get("account"));
    }

    @Test
    public void createsAccountWithInitialAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/accounts")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("initialBalance", "100")
                )
                .asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertNotNull(json.get("account"));

        double amount = Double.parseDouble(Helpers.getAccountBalance(json.getString("account")));
        assertEquals(0, Double.compare(amount, 100));
    }

    @Test
    public void doesNotCreateAccountWithNegativeInitialAmount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.post("http://localhost:8080/accounts")
                .header("accept", "application/json")
                .body(new JSONObject()
                        .put("initialBalance", "-100")
                )
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.NEGATIVE_AMOUNT, json.get("message"));
    }
}
