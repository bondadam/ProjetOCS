package com.example.sac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView position_display;
    private Button sendBtn;
    private Button orangeBtn;
    private Button redBtn;
    private Button clignotantsLeftBtn;
    private Button clignotantsRightBtn;
    private Button newServerBtn;
    private EditText address;

    private LocationManager locationManager;
    private LocationListener sacListener;
    private String ORS_API_KEY = "5b3ce3597851110001cf6248617d247385594bcc9082b43fbdb1be4e";

    // MQTT TOPICS
    private static String CLIGNOTANTS_COLOR = "clignotants/color";
    private static String CLIGNOTANTS_LEFT = "clignotants/left";
    private static String CLIGNOTANTS_RIGHT = "clignotants/right";


    private static String NAVIGATION = "navigation";
    private MqttAndroidClient mqttAndroidClient;

    private String serverUri = "tcp://192.168.43.146:1883";

    private double longitude;
    private double latitude;

    private EditText longitudeView;
    private EditText latitudeView;

    private int currentInstructionNumber;
    private TextView currentInstructionView;
    private Button nextInstructionBtn;

    private ArrayList<HashMap<String, String>> instructions;

    private String clientId = "ExampleAndroidClient";
    private ArrayList<String> topicsToSubscribeTo = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.position_display = findViewById(R.id.position_display);
        this.sendBtn = findViewById(R.id.send_button);
        this.orangeBtn = findViewById(R.id.orangeBtn);
        this.redBtn = findViewById(R.id.redBtn);
        this.clignotantsLeftBtn = findViewById(R.id.clignotantsLeftBtn);
        this.newServerBtn = findViewById(R.id.newServerBtn);
        this.address = findViewById(R.id.address);
        this.currentInstructionNumber = 0;
        this.latitudeView = findViewById(R.id.latitude);
        this.longitudeView = findViewById(R.id.longitude);
        this.currentInstructionView = findViewById(R.id.currentInstruction);
        this.nextInstructionBtn = findViewById(R.id.nextBtn);
        this.instructions = new ArrayList<>();

        Intent intent = getIntent();

        // Get the extras (if there are any)
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("server_uri")) {
                String uri = extras.getString("server_uri", "");
                if (uri != ""){
                    this.serverUri = uri;
                    this.address.setText(uri);
                }
                extras.clear();
                // TODO: Do something with the value of isNew.
            }
        }

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.sacListener = new SacLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        } else {
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this.sacListener);
        }

        redBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publishMessage(CLIGNOTANTS_COLOR, "red");
            }
        });

        orangeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publishMessage(CLIGNOTANTS_COLOR, "orange");
            }
        });

        clignotantsLeftBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publishMessage(CLIGNOTANTS_LEFT, "1");
            }
        });

        clignotantsRightBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publishMessage(CLIGNOTANTS_RIGHT, "1");
            }
        });

        newServerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String new_server_uri = address.getText().toString();
                Intent intent = getIntent();
                intent.putExtra("server_uri", new_server_uri);
                finish();
                startActivity(intent);
            }
        });

        nextInstructionBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // first send current instruction

                currentInstructionNumber++;
                if (currentInstructionNumber >= instructions.size()){
                    currentInstructionView.setText("You are at your destination!");
                } else {
                    HashMap<String, String> instruction  = instructions.get(currentInstructionNumber);
                    currentInstructionView.setText(instruction.get("instruction"));
                    //publishMessage(NAVIGATION, String.format("%s/%s/%s}", instruction.get("instruction"), instruction.get("type"), instruction.get("exit_number")));
                    publishMessage(NAVIGATION, String.format("%s,%s}", instruction.get("type"), instruction.get("exit_number")));
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String destination_latitude = latitudeView.getText().toString();
                String destination_longitude = longitudeView.getText().toString();
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = String.format("https://api.openrouteservice.org/v2/directions/driving-car?api_key=%s&start=%f,%f&end=%s,%s", ORS_API_KEY, longitude, latitude, destination_longitude, destination_latitude);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getApplicationContext(), "Instructions to destination received." , Toast.LENGTH_SHORT).show();
                                //textView.setText("Response is: "+ response.substring(0,500));
                                JSONObject jsObj = null;
                                instructions = new ArrayList<>();
                                currentInstructionNumber = 0;
                                try {
                                    jsObj = new JSONObject(response);
                                    JSONObject feature = jsObj.getJSONArray("features").getJSONObject(0);
                                    JSONArray steps = feature.getJSONObject("properties").getJSONArray("segments").getJSONObject(0).getJSONArray("steps");
                                    for (int i = 0; i < steps.length(); i++) {
                                        HashMap<String, String> step = new HashMap<>();
                                        JSONObject obj = steps.getJSONObject(i);
                                        step.put("instruction", obj.getString("instruction"));
                                        step.put("type", Integer.toString(obj.getInt("type")));
                                        step.put("exit_number", Integer.toString(obj.optInt("exit_number", -1)));
                                        instructions.add(step);
                                    }
                                    currentInstructionView.setText(instructions.get(0).get("instruction"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error while handling instructions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error getting instructions: " + error.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });

// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        });

        // MQTT

        //this.topicsToSubscribeTo.add(CLIGNOTANTS_COLOR);
        //this.topicsToSubscribeTo.add(NAVIGATION);


        clientId = clientId + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Toast.makeText(getApplicationContext(), "Reconnected to : " + serverURI , Toast.LENGTH_SHORT).show();
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopics();
                } else {
                    Toast.makeText(getApplicationContext(), "Connected to : " + serverURI , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getApplicationContext(), "Connection lost.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast.makeText(getApplicationContext(), "Incoming message: " + new String(message.getPayload()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toast.makeText(getApplicationContext(), "Delivery complete.", Toast.LENGTH_SHORT).show();
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    for (String topic : topicsToSubscribeTo){
                        subscribeToTopics();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed to connect to: " + serverUri, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic(String subscriptionTopic){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(), "Subscribed to: " + subscriptionTopic, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed to subscribe to: " + subscriptionTopic, Toast.LENGTH_SHORT).show();
                }
            });

            /*
            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });*/

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void subscribeToTopics(){
        for (String topic : topicsToSubscribeTo){
            subscribeToTopic(topic);
        }
    }

    public void publishMessage(String publishTopic, String publishMessage){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Toast.makeText(getApplicationContext(), "Message published.", Toast.LENGTH_SHORT).show();
            if(!mqttAndroidClient.isConnected()){
                Toast.makeText(getApplicationContext(), mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.", Toast.LENGTH_SHORT).show();
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // All permissions have been granted
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this.sacListener);
        }
    }


    private class SacLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            position_display.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }



}

