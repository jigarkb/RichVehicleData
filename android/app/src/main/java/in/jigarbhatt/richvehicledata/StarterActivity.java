package in.jigarbhatt.richvehicledata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StarterActivity extends AppCompatActivity {
    private static final String TAG = "StarterActivity";

    public String auth_token;
    public String user_id;
    public Button mStartCaptureButton;
    public String full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        Intent intent = getIntent();
        full_name = intent.getStringExtra("full_name");
        auth_token = intent.getStringExtra("auth_token");
        user_id = intent.getStringExtra("user_id");
        TextView tvWelcomeMsg = findViewById(R.id.tvWelcomeMsg);
        String message = "Welcome! " + full_name;
        tvWelcomeMsg.setText(message);
        mStartCaptureButton = findViewById(R.id.bStartCapture);
        mStartCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StarterActivity.this, CaptureActivity.class);
                intent.putExtra("full_name", full_name);
                intent.putExtra("auth_token", auth_token);

                StarterActivity.this.startActivity(intent);
            }
        });

    }


}
