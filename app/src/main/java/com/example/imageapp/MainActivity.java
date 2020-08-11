package com.example.imageapp;

import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imageapp.AsyncRestClient.AsyncRestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;
    private TextView textView;
    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        this.textView = (TextView) this.findViewById(R.id.textView);

        this.textView.setText(" ");

        Calendar cal = Calendar.getInstance();



        Date c = cal.getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("EEEE, d MMM yyyy", Locale.FRENCH);
        String formattedDate = df.format(c);
        SimpleDateFormat hf = new SimpleDateFormat("HH:mm", Locale.FRENCH);
        String formattedHeure = hf.format(c);
        Integer hours = cal.get(Calendar.HOUR);
        Integer am_pm = cal.get(Calendar.AM_PM);


        this.textView.setText(" " +
                "Date : " + formattedDate + "\n" +
                " Heure : " + formattedHeure+"\n");




        String longitude = "2.3488";
        String latitude = "48.8534";
        loadJson(this.textView, this.imageView , hours, am_pm ,longitude , latitude , formattedDate , formattedHeure );


    }



    private void loadJson(final TextView textview,final ImageView image ,final int hours ,final int am_pm , final String lon , final String lat ,final String DateDay ,final String HeureDay) {
        AsyncRestClient arc = new AsyncRestClient(this);

        arc.setOnReceiveDataListener(new AsyncRestClient.OnReceiveDataListener() {
            @Override
            public void onReceiveData(JSONObject jsonObject )  {

                String sunset = "";
                String sunrise = "";
                try {
                    JSONObject json_res = jsonObject.getJSONObject("results");
                    sunset = json_res.getString("sunset");
                    sunrise = json_res.getString("sunrise");
                } catch (JSONException e) {
                    e.printStackTrace();
                }




                DateFormat formatter = new SimpleDateFormat("hh:mm:ss ");
                Date dateSunset = null;
                Date dateSunrise = null;
                try {
                    dateSunset = (Date)formatter.parse(sunset);
                    dateSunrise = (Date)formatter.parse(sunrise);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Integer hours_sunset = dateSunset.getHours() ;
                Integer hours_sunrise = dateSunrise.getHours() ;

                image.setImageResource(R.drawable.sun);
                if (am_pm == Calendar.AM) {

                    if (hours < 11 && hours > hours_sunrise) {
                        image.setImageResource(R.drawable.sunrise);
                    }
                } else {

                    if (hours > hours_sunset) {
                        image.setImageResource(R.drawable.sunset);
                    }
                }




            }
        });
        arc.execute(
                new Pair<String,String>("HTTP_METHOD", "GET"),
                new Pair<String,String>("HTTP_URL", "https://api.sunrise-sunset.org/json"),
                new Pair<String,String>("lat", lat),
                new Pair<String,String>("lng", lon),
                new Pair<String,String>("date", "today")
        );
    }





}
