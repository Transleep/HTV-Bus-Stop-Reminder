package com.mms.busstopreminder;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Created by Ming on 2017-01-07.
 */
public class ServerCaller extends AsyncTask<String, Void, ServerResponse> {
	public ServerCaller(){

	}

	@Override
	protected ServerResponse doInBackground(String... params) {
		URL url;
		HttpURLConnection urlConnection = null;
		String buffer = "";
		try {
			String urlBase = "http://htv-bn.ml";
			String reqStr = "/stop_location?stop_code="+params[0];

			url = new URL(urlBase+reqStr);

			urlConnection = (HttpURLConnection) url
					.openConnection();
			InputStream in = urlConnection.getInputStream();
			InputStreamReader isw = new InputStreamReader(in);

			int data = isw.read();
			while (data != -1) {
				char current = (char) data;
				data = isw.read();
				buffer += current;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(buffer);
		} catch (final JSONException e) {
			Log.e("ERROR", "Json parsing error: " + e.getMessage());
			/*runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/*Toast.makeText(getApplicationContext(),
							"Json parsing error: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			});*/
		}

		//buffer has the returned JSON string
		return new ServerResponse(jsonObject);
	}
}