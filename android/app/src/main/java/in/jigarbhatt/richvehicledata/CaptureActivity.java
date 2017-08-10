package in.jigarbhatt.richvehicledata;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.openxc.VehicleManager;
import com.openxc.sinks.DataSinkException;

public class CaptureActivity extends AppCompatActivity {
    private static final String TAG = "CaptureActivity";
    private VehicleManager mVehicleManager;
    public AuthUploaderSink mUploaderSink;
    public Button mStopCaptureButton;
    public String auth_token;
    public String full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Intent intent = getIntent();
        full_name = intent.getStringExtra("full_name");
        auth_token = intent.getStringExtra("auth_token");
        mStopCaptureButton = findViewById(R.id.bStopCapture);
        mStopCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Unbinding from Vehicle Manager");
                mVehicleManager.removeSink(mUploaderSink);
                unbindService(mConnection);
                mVehicleManager = null;
                Intent intent = new Intent(CaptureActivity.this, StarterActivity.class);
                intent.putExtra("full_name", full_name);
                intent.putExtra("auth_token", auth_token);

                CaptureActivity.this.startActivity(intent);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            mVehicleManager.removeSink(mUploaderSink);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences("MyPref", 0);
        String auth_token = settings.getString("auth_token", "unknown");

        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        if(mUploaderSink != null) {
            mVehicleManager.removeSink(mUploaderSink);
        }
        try {
            mUploaderSink = new AuthUploaderSink(getApplicationContext(), "https://brainstorm-cloud.appspot.com/openxc_stats/add_to_pull", auth_token);
        } catch (DataSinkException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addSink(mUploaderSink);

        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
