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
public class AccountDeleteTest extends BaseTest {
    @Before
    public void createAccount() throws Exception {
        accounts.add(Helpers.createAccount("150"));
    }

    @Test
    public void properlyDelete() throws Exception {
        HttpResponse<JsonNode> res = Unirest.delete("http://localhost:8080/accounts/" + accounts.get(0))
                .header("accept", "application/json")
                .asJson();

        assertEquals(200, res.getStatus());
        assertEquals(false, Helpers.accountExists(accounts.get(0)));
    }


    @Test
    public void invalidAccountNumber() throws Exception {
        HttpResponse<JsonNode> res = Unirest.put("http://localhost:8080/accounts/INVALID_NUMBER")
                .header("accept", "application/json")
                .asJson();

        assertEquals(400, res.getStatus());
        JSONObject json = res.getBody().getObject();
        assertEquals(Main.Messages.INVALID_ACCOUNT_NUMBER, json.getString("message"));
    }
}
