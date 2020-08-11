package com.example.imageapp.AsyncRestClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.example.imageapp.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;


public class AsyncRestClient extends AsyncTask<Pair<String, String>, Void, JSONObject>
{
    private ProgressDialog progressDialog;
    private Context context;
    private String error;

    private OnReceiveDataListener onReceiveDataListener;

    public interface OnReceiveDataListener {
        public void onReceiveData(JSONObject jsonObject);
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }

    public AsyncRestClient(Context cx) {
        context = cx;
        error   = new String();

        progressDialog = new ProgressDialog(cx);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(cx.getString(R.string.arc_pgd_title));
        progressDialog.setMessage(cx.getString(R.string.arc_pgd_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }


    @Override
    protected JSONObject doInBackground(Pair<String, String>... pairs) {

        JSONObject result   = new JSONObject();
        String query        = new String();
        String query_method = null;
        String query_url    = null;

        for (int i=0; i<pairs.length; i++) {
            if(pairs[i].first.equals("HTTP_URL")) {
                query_url = pairs[i].second;
                continue;
            }

            if(pairs[i].first.equals("HTTP_METHOD")) {
                query_method = pairs[i].second.toUpperCase();
                continue;
            }

            if(!query.isEmpty()){
                query += "&";
            }

            try {
                query += URLEncoder.encode(pairs[i].first, "UTF-8") + "=" + URLEncoder.encode(pairs[i].second, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        try {
            URL url = new URL(query_url);
            if(query_method.equals("GET") || query_method.equals("DELETE")){
                url = new URL(query_url + "?" + query);
            }

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod(query_method);
            httpsURLConnection.setInstanceFollowRedirects(false);
            httpsURLConnection.setConnectTimeout(5000);
            httpsURLConnection.setReadTimeout(5000);
            httpsURLConnection.setUseCaches(false);

            if(!query_method.equals("GET") && !query_method.equals("DELETE")) {
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                OutputStream outputStream = httpsURLConnection.getOutputStream();
                outputStream.write(query.getBytes("UTF-8"));
                outputStream.close();
            }

            InputStream inputStream = httpsURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            inputStream.close();

            result = new JSONObject(response);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            error = context.getString(R.string.arc_error_url);
        } catch (IOException e) {
            e.printStackTrace();
            error = context.getString(R.string.arc_error_io);
        } catch (JSONException e) {
            e.printStackTrace();
            error = context.getString(R.string.arc_error_json);
        }

        return result;
    }


    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);

        if(!error.isEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }

        if(onReceiveDataListener!=null){
            onReceiveDataListener.onReceiveData(jsonObject);
        }

        progressDialog.dismiss();

    }
}