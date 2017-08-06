package in.jigarbhatt.richvehicledata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class UserAreaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        Intent intent = getIntent();
        String full_name = intent.getStringExtra("full_name");

        TextView tvWelcomeMsg = (TextView) findViewById(R.id.tvWelcomeMsg);

        // Display user details
        String message = full_name + " welcome to your user area";
        tvWelcomeMsg.setText(message);
    }
}
