import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
public class AccountNumberGenerator {
    static private SecureRandom random = new SecureRandom();

    public static String generateAccountNumber() {
        return new BigInteger(130, random).toString(32);
    }
}
