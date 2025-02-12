import com.yukibytes.utils.mc.msa.MSAccountAuthenticator;
import com.yukibytes.utils.mc.msa.MSAccountData;
import com.yukibytes.utils.mc.msa.MSAuthConfig;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author addo6544
 * 2025/2/10 17:49
 **/
public class Run {
    public static void main(String[] args) {
        try {
            JSONObject res = ((JSONObject) new JSONTokener(new FileReader(new File("D:/@Addo6544/str.json"))).nextValue()).getJSONObject("YukiMCL");
            MSAuthConfig c = new MSAuthConfig(
                    res.getString("client_id"),
                    res.getInt("port")
            );
            MSAccountAuthenticator A = new MSAccountAuthenticator(c);
            MSAccountData acc = A.authenticate().get();
            System.out.println("==============================");
            //MSAccountData b = new MSAccountAuthenticator(c).refreshLogin(a.getRefreshToken()).get();
            System.out.println(acc.getUsername());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
