package in.jigarbhatt.richvehicledata;

import com.google.gson.JsonObject;

import java.util.Queue;

/**
 * Created by jigarkb on 8/5/17.
 */

public class QueueManager {
    private static QueueManager ourInstance = null;

    public Queue<JsonObject> data_queue;

    public static synchronized QueueManager getInstance() {
        if(null == ourInstance){
            ourInstance = new QueueManager();
        }
        return ourInstance;
    }

    public QueueManager() {
    }
}
