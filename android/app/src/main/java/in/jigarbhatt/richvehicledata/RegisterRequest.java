package in.jigarbhatt.richvehicledata;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    private static final String REGISTER_REQUEST_URL = "https://brainstorm-cloud.appspot.com/user_account/register";
    private Map<String, String> params;

    public RegisterRequest(String full_name, String user_id, String password, Response.Listener<String> listener) {
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("full_name", full_name);
        params.put("user_id", user_id);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
