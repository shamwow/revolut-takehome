import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class AccountsGetTest extends BaseTest {
    @Test
    public void getMultipleAccounts() throws Exception {
        accounts.add(Helpers.createAccount());
        accounts.add(Helpers.createAccount("100"));

        HttpResponse<JsonNode> res = Unirest.get("http://localhost:8080/accounts").asJson();

        assertEquals(200, res.getStatus());
        JSONObject json = res.getBody().getObject().getJSONObject("accounts");
        assertEquals(true, json.has(accounts.get(0)));
        assertEquals(true, json.has(accounts.get(1)));

        double amount1 = Double.parseDouble(json.getString(accounts.get(0)));
        double amount2 = Double.parseDouble(json.getString(accounts.get(1)));
        assertEquals(0, Double.compare(amount1, 0));
        assertEquals(0, Double.compare(amount2, 100));
    }
}
