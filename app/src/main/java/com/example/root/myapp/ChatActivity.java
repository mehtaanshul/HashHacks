package com.example.root.myapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.lujun.lmbluetoothsdk.BluetoothController;
import co.lujun.lmbluetoothsdk.base.BluetoothListener;
import co.lujun.lmbluetoothsdk.base.State;

/**
 * Author: lujun(http://blog.lujun.co)
 * Date: 2016-1-21 16:10
 */
public class ChatActivity extends Activity {
    private BluetoothController mBluetoothController;

    private Button btnDisconnect, btnSend;
    private EditText etSend;
    private TextView tvConnectState, tvContent, tvDeviceName, tvDeviceMac;

    private int mConnectState;
    private String mMacAddress = "", mDeviceName = "";

    private static final String TAG = "LMBluetoothSdk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);

        init();
    }

    private void init(){
        mMacAddress = getIntent().getStringExtra("mac");
        mDeviceName = getIntent().getStringExtra("name");

        mBluetoothController = BluetoothController.getInstance();
        mBluetoothController.setBluetoothListener(new BluetoothListener() {
            @Override
            public void onActionStateChanged(int preState, int state) {
                Toast.makeText(ChatActivity.this, "BT state: " + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActionDiscoveryStateChanged(String discoveryState) {}

            @Override
            public void onActionScanModeChanged(int preScanMode, int scanMode) {}

            @Override
            public void onBluetoothServiceStateChanged(final int state) {
                // If you want to update UI, please run this on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectState = state;
                        tvConnectState.setText("Connection state: " + Utils.transConnStateAsString(state));
                    }
                });
            }

            @Override
            public void onActionDeviceFound(BluetoothDevice device, short rssi) {}

            @Override
            public void onReadData(final BluetoothDevice device, final byte[] data) {
                // If you want to update UI, please run this on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String deviceName = device == null ? "" : device.getName();
                        tvContent.append(deviceName + ": " + new String(data) + "\n");
                    }
                });
            }
        });

        btnSend = (Button) findViewById(R.id.btn_send);
        btnDisconnect = (Button) findViewById(R.id.btn_disconnect);
        tvConnectState = (TextView) findViewById(R.id.tv_connect_state);
        etSend = (EditText) findViewById(R.id.et_send_content);
        tvContent = (TextView) findViewById(R.id.tv_chat_content);
        tvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        tvDeviceMac = (TextView) findViewById(R.id.tv_device_mac);

        tvDeviceName.setText("Device: " + mDeviceName);
        tvDeviceMac.setText("MAC address: " + mMacAddress);
        tvConnectState.setText("Connection state: "
                + Utils.transConnStateAsString(mBluetoothController.getConnectionState()));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etSend.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    return;
                }
                String tag_json_obj = "register_req";
                String url = "http://192.168.43.197/hashhacks/public/issue_book";
                Log.e("chat","hey");
                Map<String, String> params = new HashMap<String, String>();
                params.put("roll_no", msg);
                params.put("book_id", "8");
                params.put("tag_id", "8");

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");

                                    Log.d(TAG, "onResponse: " + status);
                                    if (status.equals("ok")) {

                                        Toast.makeText(ChatActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                                //Toast.makeText(CouponForm.this, "Connection failed :/", Toast.LENGTH_SHORT).show();
                            }

                });

                // Adding request to request queue
                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, 3, 2));
                AppController.getInstance().addToRequestQueue(jsonObjReq);

                mBluetoothController.write(msg.getBytes());
                tvContent.append("Me: " + msg + "\n");
                etSend.setText("");
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnectState == State.STATE_CONNECTED) {
                    mBluetoothController.disconnect();
                }
                finish();
            }
        });

        if (!TextUtils.isEmpty(mMacAddress)) {
            mBluetoothController.connect(mMacAddress);
        }else {
            if (mBluetoothController.getConnectedDevice() == null){
                return;
            }
            mDeviceName = mBluetoothController.getConnectedDevice().getName();
            mMacAddress = mBluetoothController.getConnectedDevice().getAddress();
            tvDeviceName.setText("Device: " + mDeviceName);
            tvDeviceMac.setText("MAC address: " + mMacAddress);
        }
    }
}
