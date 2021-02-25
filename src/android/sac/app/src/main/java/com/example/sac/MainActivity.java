package com.example.sac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView position_display;
    private Button send_button;
    private Button orangeBtn;
    private Button redBtn;

    private LocationManager locationManager;
    private static String CLIGNOTANTS_COLOR = "clignotants/color";
    private MqttAndroidClient mqttAndroidClient;

    private final String serverUri = "tcp://192.168.0.111:1883";

    private String clientId = "ExampleAndroidClient";
    private ArrayList<String> topicsToSubscribeTo = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.position_display = findViewById(R.id.position_display);
        this.send_button = findViewById(R.id.send_button);
        this.orangeBtn = findViewById(R.id.orangeBtn);
        this.redBtn = findViewById(R.id.redBtn);

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            //this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
            //this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this::onLocationChanged);
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

        // MQTT

        this.topicsToSubscribeTo.add(CLIGNOTANTS_COLOR);

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
            //this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this::onLocationChanged);
        }
    }


    public void onLocationChanged(Location location) {
        this.position_display.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }


}