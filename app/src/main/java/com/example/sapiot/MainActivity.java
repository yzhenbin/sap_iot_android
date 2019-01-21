package com.example.sapiot;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.sapiot.model.Measure;
import com.example.sapiot.model.Post;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    public static final String CERTIFICATE_SECRET = "";

    public static final String BASE_URL = "https://xxxxxxx.us10.cp.iot.sap/iot/gateway/rest/measures/";
    public static final String DEVICE_ID = "";
    public static final String CAPABILITY_ALTERNATE_ID = "";
    public static final String SENSOR_ALTERNATE_ID = "0:0:0:0";

    //Customize this according to model class
    public static final String TEMPERATURE = "58.7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load certificate file from resource
     //CHANGE FILE
        InputStream caInput = getResources().openRawResource(R.raw.sap_android);

        //Create a KeyStore based on the certificate file
        String keyStoreType = "PKCS12";
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(caInput, CERTIFICATE_SECRET.toCharArray());

            //Load default CA certificates
            KeyStore defaultCAs = KeyStore.getInstance("AndroidCAStore");

            //Add default CA certificates into earlier created KeyStore
            if (defaultCAs != null) {
                defaultCAs.load(null, null);

                Enumeration<String> keyAliases = defaultCAs.aliases();

                while (keyAliases.hasMoreElements()) {
                    String alias = keyAliases.nextElement();
                    Certificate cert = defaultCAs.getCertificate(alias);

                    if (!keyStore.containsAlias(alias))
                        keyStore.setCertificateEntry(alias, cert);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Create a TrustManager based on the KeyStore (SAP Certificate + Default)
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {

            // Load key from certificate file into KeyManagerFactory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, CERTIFICATE_SECRET.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            //Create SSL context for HTTPS communication
            SSLContext sslC = SSLContext.getInstance("TLS");
            sslC.init(keyManagers,tmf.getTrustManagers() , null);

            //Setup OkHttpClient (Need to do this to perform SSL certificate authentication)
            OkHttpClient client = new OkHttpClient();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client = client.newBuilder()
                    .sslSocketFactory(sslC.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                    .build();

            //Build JSON request
            Post postBody = new Post();
            postBody.setCapabilityAlternateId(CAPABILITY_ALTERNATE_ID);
            postBody.setSensorAlternateId(SENSOR_ALTERNATE_ID);

            final Measure m = new Measure();
            m.setTemperature(TEMPERATURE);
            postBody.setMeasures(new ArrayList<Measure>(){{
                add(m);
            }});

            String jsonInString = new Gson().toJson(postBody);
            System.out.println(jsonInString);
            JSONObject json = new JSONObject(jsonInString);

            MediaType JSON = MediaType.parse("application/json");
            okhttp3.RequestBody body = RequestBody.create(JSON, json.toString());
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL+DEVICE_ID)
                    .post(body)
                    .build();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            okhttp3.Response response = client.newCall(request).execute();

            String networkResp = response.body().string();
            Log.e("Response: ", networkResp);
            TextView tv1 = findViewById(R.id.txtResponse);
            tv1.setText(networkResp);


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static JSONObject parseJSONStringToJSONObject(final String strr) {

        JSONObject response = null;
        try {
            response = new JSONObject(strr);
        } catch (Exception ex) {
            Log.e("Could not parse JSON: ", strr);
            try {
                response = new JSONObject();
                response.put("result", "failed");
                response.put("data", strr);
                response.put("error", ex.getMessage());
            } catch (Exception exx) {
            }
        }
        Log.e("JSON:", strr);
        return response;
    }

}
