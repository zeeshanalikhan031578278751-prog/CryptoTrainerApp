package com.example.cryptotrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**

MainActivity for the CryptoTrainer app.

This activity fetches the live price of BTC/USDT from the Binance API

and displays it, updating automatically every 5 seconds.

Required dependencies:

1. OkHttp for network requests. Add the following to your app-level build.gradle file:



implementation("com.squareup.okhttp3:okhttp:4.12.0")

Required permissions:

1. Internet access. Add the following to your AndroidManifest.xml file:



<uses-permission android:name="android.permission.INTERNET" />  
*/
public class MainActivity extends AppCompatActivity {

private static final String TAG = "MainActivity";  
private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";  
private static final int REFRESH_INTERVAL_MS = 5000; // 5 seconds  

private TextView priceTextView;  
private final OkHttpClient client = new OkHttpClient();  
private final Handler handler = new Handler(Looper.getMainLooper());  
private Runnable priceUpdaterRunnable;  

@Override  
protected void onCreate(Bundle savedInstanceState) {  
    super.onCreate(savedInstanceState);  
    setContentView(R.layout.activity_main);  

    // Initialize the TextView from the layout  
    priceTextView = findViewById(R.id.priceText);  

    // Define the Runnable that will fetch the price periodically  
    priceUpdaterRunnable = new Runnable() {  
        @Override  
        public void run() {  
            fetchBitcoinPrice();  
            // Schedule the same runnable to run again after the specified interval  
            handler.postDelayed(this, REFRESH_INTERVAL_MS);  
        }  
    };  
}  

@Override  
protected void onResume() {  
    super.onResume();  
    // Start the periodic updates when the activity is resumed  
    startPriceUpdates();  
}  

@Override  
protected void onPause() {  
    super.onPause();  
    // Stop the periodic updates when the activity is paused to save resources  
    stopPriceUpdates();  
}  

private void startPriceUpdates() {  
    // Remove any existing callbacks to prevent duplicates  
    handler.removeCallbacks(priceUpdaterRunnable);  
    // Post the runnable to start the updates  
    handler.post(priceUpdaterRunnable);  
}  

private void stopPriceUpdates() {  
    // Remove the callbacks to stop the updates  
    handler.removeCallbacks(priceUpdaterRunnable);  
}  

private void fetchBitcoinPrice() {  
    // Build the request for the Binance API  
    Request request = new Request.Builder()  
            .url(BINANCE_API_URL)  
            .build();  

    // Execute the request asynchronously  
    client.newCall(request).enqueue(new Callback() {  
        @Override  
        public void onFailure(Call call, IOException e) {  
            // Handle API call failure  
            Log.e(TAG, "API call failed: ", e);  
            updatePriceTextView("Error loading price");  
        }  

        @Override  
        public void onResponse(Call call, Response response) throws IOException {  
            if (response.isSuccessful()) {  
                final String responseBody = response.body().string();  
                try {  
                    // Parse the JSON response  
                    JSONObject jsonObject = new JSONObject(responseBody);  
                    String price = jsonObject.getString("price");  
                      
                    // Format the price for better readability  
                    double priceValue = Double.parseDouble(price);  
                    DecimalFormat df = new DecimalFormat("#,##0.00");  
                    final String formattedPrice = "BTC/USDT: $" + df.format(priceValue);  
                      
                    updatePriceTextView(formattedPrice);  

                } catch (JSONException e) {  
                    Log.e(TAG, "JSON parsing error: ", e);  
                    updatePriceTextView("Error parsing data");  
                }  
            } else {  
                // Handle unsuccessful API response (e.g., 404, 500)  
                Log.e(TAG, "API response unsuccessful. Code: " + response.code());  
                updatePriceTextView("Error loading price");  
            }  
        }  
    });  
}  

/**  
 * Updates the price TextView on the main UI thread.  
 * @param text The text to display in the TextView.  
 */  
private void updatePriceTextView(final String text) {  
    // Any UI update must be done on the main thread.  
    // We use runOnUiThread to ensure this.  
    runOnUiThread(() -> {  
        if (priceTextView != null) {  
            priceTextView.setText(text);  
        }  
    });  
}

}
