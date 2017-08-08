package in.jigarbhatt.richvehicledata;

import android.content.Context;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;
import com.openxc.sinks.ContextualVehicleDataSink;
import com.openxc.sinks.DataSinkException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by jigarkb on 8/8/17.
 */

public class AuthUploaderSink extends ContextualVehicleDataSink {
    private final static String TAG = "AuthUploaderSink";
    private final static int UPLOAD_BATCH_SIZE = 25;
    private final static int MAXIMUM_QUEUED_RECORDS = 5000;
    private final static int HTTP_TIMEOUT = 5000;

    private String mAuthToken;
    private URI mUri;
    private BlockingQueue<VehicleMessage> mRecordQueue =
            new LinkedBlockingQueue<>(MAXIMUM_QUEUED_RECORDS);
    private Lock mQueueLock = new ReentrantLock();
    private Condition mRecordsQueued = mQueueLock.newCondition();
    private UploaderThread mUploader = new UploaderThread();

    /**
     * Initialize and start a new AuthUploaderSink immediately.
     *
     * @param uri the URI to send HTTP POST requests to with the JSON data.
     */
    public AuthUploaderSink(Context context, URI uri, String auth_token) {
        super(context);
        mUri = uri;
        mAuthToken = auth_token;
    }

    public AuthUploaderSink(Context context, String path, String auth_token) throws DataSinkException {
        this(context, uriFromString(path), auth_token);
    }

    @Override
    public void stop() {
        mUploader.done();
    }

    @Override
    public void receive(VehicleMessage message) {
        mRecordQueue.offer(message);
        if(mRecordQueue.size() >= UPLOAD_BATCH_SIZE) {
            try {
                mQueueLock.lock();
                mRecordsQueued.signal();
            } finally {
                mQueueLock.unlock();
            }
        }
    }

    /**
     * Returns true if the path is not null and if it is a valid URI.
     *
     * @param path a URI to validate
     * @return true if path is a valid URI.
     *
     */
    public static boolean validatePath(String path) {
        if(path == null) {
            Log.w(TAG, "Uploading path not set");
            return false;
        }

        try {
            uriFromString(path);
            return true;
        } catch(DataSinkException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uri", mUri)
                .add("queuedRecords", mRecordQueue.size())
                .toString();
    }

    private static URI uriFromString(String path) throws DataSinkException {
        try {
            return new URI(path);
        } catch(java.net.URISyntaxException e) {
            throw new UploaderException(
                    "Uploading path in wrong format -- expected: ip:port");
        }
    }

    private static class UploaderException extends DataSinkException {
        private static final long serialVersionUID = 7436279598279767619L;

        public UploaderException() { }

        public UploaderException(String message) {
            super(message);
        }
    }

    private class UploaderThread extends Thread {
        private boolean mRunning = true;

        public UploaderThread() {
            start();
        }

        @Override
        public void run() {
            while(mRunning) {
                try {
                    ArrayList<VehicleMessage> records = getRecords();
                    String data = JsonFormatter.serialize(records);
                    HttpPost request = constructRequest(data);
                    makeRequest(request);
                } catch(UploaderException e) {
                    Log.w(TAG, "Problem uploading the record", e);
                } catch(InterruptedException e) {
                    Log.w(TAG, "Uploader was interrupted", e);
                    break;
                }
            }
        }

        public void done() {
            mRunning = false;
        }

        private HttpPost constructRequest(String data)
                throws UploaderException {
            HttpPost request = new HttpPost(mUri);
            request.addHeader("Authorization", "Basic "+mAuthToken);
            try {
                ByteArrayEntity entity = new ByteArrayEntity(
                        data.getBytes("UTF8"));
                entity.setContentEncoding(
                        new BasicHeader("Content-Type", "application/json"));
                request.setEntity(entity);
            } catch(UnsupportedEncodingException e) {
                Log.w(TAG, "Couldn't encode records for uploading", e);
                throw new UploaderException();
            }
            return request;
        }

        private void makeRequest(HttpPost request) throws InterruptedException {
            HttpParams parameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(parameters, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(parameters, HTTP_TIMEOUT);
            final HttpClient client = new DefaultHttpClient(parameters);
            try {
                HttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                Log.d(TAG, "Request executed with status code: "+statusCode);
                if(statusCode != HttpStatus.SC_CREATED) {
                    Log.w(TAG, "Got unexpected status code: " + statusCode);
                }
            } catch(IOException e) {
                Log.w(TAG, "Problem uploading the record", e);
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2) {
                    Log.w(TAG, "Uploader interrupted after an error", e2);
                    throw e2;
                }
            }
        }

        private ArrayList<VehicleMessage> getRecords() throws InterruptedException {
            try {
                mQueueLock.lock();
                while(mRecordQueue.isEmpty()) {
                    // the queue is already thread safe, but we use this lock to get
                    // a condition variable we can use to signal when a batch has
                    // been queued.
                    mRecordsQueued.await(5, TimeUnit.SECONDS);
                }

                ArrayList<VehicleMessage> records = new ArrayList<>();
                mRecordQueue.drainTo(records, UPLOAD_BATCH_SIZE);
                return records;
            } finally {
                mQueueLock.unlock();
            }
        }
    }


}
