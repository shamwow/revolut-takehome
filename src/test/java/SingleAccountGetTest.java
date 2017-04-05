import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class SingleAccountGetTest extends BaseTest {
    @Test
    public void getNonexistentAccount() throws Exception {
        HttpResponse<JsonNode> res = Unirest.get("http://localhost:8080/accounts/NONEXISTENT").asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INVALID_ACCOUNT_NUMBER, json.getString("message"));
    }

    @Test
    public void getActualAccount() throws Exception {
        accounts.add(Helpers.createAccount());
        accounts.add(Helpers.createAccount("100"));

        HttpResponse<JsonNode> res = Unirest.get("http://localhost:8080/accounts/" + accounts.get(0)).asJson();
        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject();
        double balance = Double.parseDouble(json.getString("balance"));
        assertEquals(0, Double.compare(balance, 0));

        res = Unirest.get("http://localhost:8080/accounts/" + accounts.get(1)).asJson();
        assertEquals(200, res.getStatus());
        json = res.getBody().getObject();
        balance = Double.parseDouble(json.getString("balance"));
        assertEquals(0, Double.compare(balance, 100));
    }
}
