package com.mms.busstopreminder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ming on 2017-01-07.
 */
public class ServerResponse {

	String respCode = "";
	JSONArray data = null;
	String message = "";

	public ServerResponse(JSONObject responseObj){
		try {
			respCode = responseObj.getString("code");
			data = responseObj.getJSONArray("data");
			message = responseObj.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getRespCode() {
		return respCode;
	}

	public JSONArray getData() {
		return data;
	}

	public String getMessage() {
		return message;
	}
}
