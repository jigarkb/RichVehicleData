package in.jigarbhatt.richvehicledata;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class VerifyTokenRequest extends StringRequest {
    private static final String LOGIN_REQUEST_URL = "https://brainstorm-cloud.appspot.com/user_account/verify";
    private Map<String, String> params;

    public VerifyTokenRequest(String auth_token, Response.Listener<String> listener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("auth_token", auth_token);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
