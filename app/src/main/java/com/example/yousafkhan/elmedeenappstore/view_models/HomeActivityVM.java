package com.example.yousafkhan.elmedeenappstore.view_models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yousafkhan.elmedeenappstore.Models.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivityVM extends AndroidViewModel {

    private static final String API_URL =
            "http://www.elmedeen.com/mobile/elmedeen_app_store/php_scripts/get_apps.php";

    public static MutableLiveData<List<App>> dataList;

    // used if server responds with an error message
    private MutableLiveData<String> server_error_message;

    public HomeActivityVM(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<App>> getDataList() {
        if (dataList == null) {
            loadAllApps();
            dataList = new MutableLiveData<>();
        }
        return dataList;
    }

    public MutableLiveData<String> getServer_error_message() {
        if(server_error_message == null) {
            server_error_message = new MutableLiveData<>();
        }
        return server_error_message;
    }

    public void loadAllApps() {
        StringRequest request = new StringRequest(
                        Request.Method.GET,
                        API_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                parseJSONResponse(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                server_error_message.postValue("Check your internet connection " +
                                        "and then try again");
                            }
                        }
                    );

        request.setRetryPolicy(new DefaultRetryPolicy(
                    7000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );


        RequestQueue requestQueue = Volley.newRequestQueue(getApplication().getApplicationContext());
        requestQueue.add(request);
    }

    private void parseJSONResponse(String jsonResponse) {
        List<App> list = new ArrayList<>();
        App singleApp;

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            String responseStatus = jsonObject.getString("status");

            if(responseStatus.equals("success")) {
                JSONArray jsonArray = jsonObject.getJSONArray("apps");

                for(int i=0; i<jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    singleApp = new App(
                            jsonObject.getString("app_name_eng"),
                            jsonObject.getString("app_name_urd"),
                            jsonObject.getString("app_name_arb"),
                            jsonObject.getString("app_description_eng"),
                            jsonObject.getString("app_description_urd"),
                            jsonObject.getString("app_description_arb"),
                            jsonObject.getString("app_apk_size"),
                            jsonObject.getString("app_package_name"),
                            jsonObject.getString("app_title_image_url"),
                            jsonObject.getString("app_version_url"),
                            jsonObject.getString("app_download_url"),
                            jsonObject.getString("app_screenshot1_url"),
                            jsonObject.getString("app_screenshot2_url")
                    );

                    list.add(singleApp);
                }

                dataList.postValue(list);

            } else {
                server_error_message.postValue("Currently no apps are available");
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
