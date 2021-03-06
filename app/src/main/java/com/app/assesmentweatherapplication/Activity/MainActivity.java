package com.app.assesmentweatherapplication.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.assesmentweatherapplication.Adapters.HourlyWeatherAdapter;
import com.app.assesmentweatherapplication.Adapters.PreviousWeatherAdapter;
import com.app.assesmentweatherapplication.Adapters.SevenDaysWeatherAdapter;
import com.app.assesmentweatherapplication.Config;
import com.app.assesmentweatherapplication.Model.SevenDaysWeather;
import com.app.assesmentweatherapplication.Model.HourlyWeather;
import com.app.assesmentweatherapplication.Model.PreviousWeather;
import com.app.assesmentweatherapplication.R;
import com.app.assesmentweatherapplication.Server.ServerCalling;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    TextView textViewCity, textViewToday, textViewDate,
            textViewTemperature, textViewTime, textViewFeels,
            textViewCountry, textViewDescription, textViewSunset,
            textViewSunrise, textViewHumidity, textViewUvi, textViewWindSpeed, textViewDewPoint ;
    ImageView imageViewWeather, imageViewSevenDays, imageViewFiveDays;
    RecyclerView recyclerViewHourly, recyclerViewSevenDays, recyclerViewPrevious;
    CardView cardViewSevenDays,cardViewFiveDays;
    HourlyWeatherAdapter hourlyWeatherAdapter;
    SevenDaysWeatherAdapter sevenDaysWeatherAdapter;
    PreviousWeatherAdapter previousWeatherAdapter;
   // RecyclerView.LayoutManager layoutManagerHourly, layoutManagerSevenDays, layoutManagerPreviousWeather;
    ArrayList<HourlyWeather> hourlyWeathers = new ArrayList<>();
    ArrayList<SevenDaysWeather> sevenDaysWeathers = new ArrayList<>();
    ArrayList<PreviousWeather> previousWeathers = new ArrayList<>();
    ArrayList<Long> previousTime = new ArrayList<>();
    Set<PreviousWeather> set;
//        previousWeathers.clear();
//        previousWeathers.addAll(set);
    //LocationManager locationManager;
    LocationListener locationListener;
    LocationManager manager;
    String TAG = getClass().getSimpleName(), latitude = "", longitude = "";
    long timezone = 0, unixDate = 0,time=0L;
    boolean isOpenSevenDays = true,isOpenFiveDays=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(this);
        recyclerViewHourly = findViewById(R.id.hourlyRecyclerView);
        recyclerViewSevenDays = findViewById(R.id.sevenDaysRecyclerView);
        recyclerViewPrevious = findViewById(R.id.previouslyRecyclerView);
        textViewCity = findViewById(R.id.cityTv);
        textViewCountry = findViewById(R.id.countryTv);
        textViewDescription = findViewById(R.id.descTv);
        textViewToday = findViewById(R.id.todayTv);
        textViewDate = findViewById(R.id.dateTv);
        textViewTemperature = findViewById(R.id.temperatureTv);
        textViewTime = findViewById(R.id.timeTv);
        textViewFeels = findViewById(R.id.feelsTv);
        imageViewWeather = findViewById(R.id.weatherIv);
        imageViewSevenDays = findViewById(R.id.openSevenDaysIv);
        imageViewFiveDays = findViewById(R.id.fiveDaysIv);
        textViewSunset = findViewById(R.id.sunsetTv);
        textViewSunrise = findViewById(R.id.sunriseTv);
        textViewUvi = findViewById(R.id.uviTv);
        textViewHumidity = findViewById(R.id.humidityTv);
        textViewWindSpeed = findViewById(R.id.windTv);
        textViewDewPoint = findViewById(R.id.dewTv);
        cardViewSevenDays = findViewById(R.id.sevenDaysCard);
        cardViewFiveDays = findViewById(R.id.fiveDaysCard);
        previousTime.clear();
        long date = new Date().getTime()/1000L;
        for (int i = 0; i <5 ; i++) {
            time=date-i*+24 * 60 * 60;
            previousTime.add(time);
        }


        cardViewSevenDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenSevenDays) {
                    recyclerViewSevenDays.setVisibility(View.VISIBLE);
                    imageViewSevenDays.setImageResource(R.drawable.ic_minus_thick_grey600_24dp);
                    imageViewSevenDays.setColorFilter(getResources().getColor(R.color.colorYellow));
                    isOpenSevenDays = false;
                } else {
                    recyclerViewSevenDays.setVisibility(View.GONE);
                    imageViewSevenDays.setImageResource(R.drawable.ic_plus_thick_grey600_24dp);
                    imageViewSevenDays.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    isOpenSevenDays = true;
                }
            }
        });
        cardViewFiveDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenFiveDays) {
                    recyclerViewPrevious.setVisibility(View.VISIBLE);
                    imageViewFiveDays.setImageResource(R.drawable.ic_minus_thick_grey600_24dp);
                    imageViewFiveDays.setColorFilter(getResources().getColor(R.color.colorYellow));
                    isOpenFiveDays = false;
                } else {
                    recyclerViewPrevious.setVisibility(View.GONE);
                    imageViewFiveDays.setImageResource(R.drawable.ic_plus_thick_grey600_24dp);
                    imageViewFiveDays.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    isOpenFiveDays = true;
                }
            }
        });
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        textViewTime.setText(new SimpleDateFormat("h:mm a").format(new Date()));
//        textViewDate.setText(new SimpleDateFormat("EEE, d MMM").format(new Date()));
        // statusCheck();
        getCurrentLocation();


    }

    private void getCurrentLocation() {

        if (isOnline()){
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else {
                //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                        getCurrentLocationWeather(latitude, longitude);
                        getHourlySevenDaysWeather(latitude, longitude);
                        getPreviousHistoricalWeather();
                    }
                };
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    return;
                }
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        5000, 1000
                        , locationListener);
            }
        }else {
            showAlertDialog();
        }

    }

    private void getPreviousHistoricalWeather() {


        previousWeathers.clear();
        //Toast.makeText(this, previousWeathers.size()+"", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < previousTime.size(); i++) {
            
            ServerCalling.getHistoricalWeather(latitude, longitude, String.valueOf(previousTime.get(i)), new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String date = unixDateConverter(response.getJSONObject("current").getLong("dt"));
                        String sunrise = unixTimeConverter(response.getJSONObject("current").getLong("sunrise"));
                        String sunset = unixTimeConverter(response.getJSONObject("current").getLong("sunset"));
                        String humidity = response.getJSONObject("current").getString("humidity")+"%";
                        String dewPoint = response.getJSONObject("current").getString("dew_point");
                        String uvi = response.getJSONObject("current").getString("uvi");
                        String wind = response.getJSONObject("current").getString("wind_speed")+" mph";
                        JSONArray hourly = response.getJSONArray("hourly");
                        String temperature = new DecimalFormat("#.#").format(response.getJSONObject("current").getDouble("temp")-273.15)+"°C";
                        String image = response.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("icon")+".png";
                        String description = response.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
                        previousWeathers.add(new PreviousWeather(date,image,temperature,description,sunrise,sunset,humidity,dewPoint,uvi,wind,hourly));
                        setPreviousWeatherAdapter();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                   // Toast.makeText(MainActivity.this, previousWeathers.size()+"", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(ANError anError) {

                }
            });

      }


    }
    public boolean isOnline() {

        //Get the connectivity service from the device
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        //Return true if there is no connectivity issues with the internet
        return info != null && info.isConnected();

    }

    private void showAlertDialog() {

        //This dialog will be shown if the device is not connected with the online
        new AlertDialog.Builder(this).setTitle("No Internet Connection")
                .setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isOnline()) {

                            //If the device is online then it can populate data from the github
                            getCurrentLocation();
                        } else {
                            finish();
                        }
                    }
                }).show();
    }

    private void getHourlySevenDaysWeather(String latitude, String longitude) {
        ServerCalling.getHourlySevenDaysWeather(latitude, longitude, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    textViewSunrise.setText(unixTimeConverter(response.getJSONObject("current").getLong("sunrise")));
                    textViewSunset.setText(unixTimeConverter(response.getJSONObject("current").getLong("sunset")));
                    textViewHumidity.setText(response.getJSONObject("current").getString("humidity") + "%");
                    textViewUvi.setText(response.getJSONObject("current").getString("uvi"));
                    textViewWindSpeed.setText(response.getJSONObject("current").getString("wind_speed")+" mph");
                    textViewDewPoint.setText(response.getJSONObject("current").getString("dew_point"));
                    JSONArray jsonArrayHourly = response.getJSONArray("hourly");
                    JSONArray jsonArrayDaily = response.getJSONArray("daily");
                    hourlyWeathers.clear();
                    for (int i = 0; i < jsonArrayHourly.length(); i++) {
                        String date = unixTimeConverter(jsonArrayHourly.getJSONObject(i).getLong("dt"));
                        String temperature = new DecimalFormat("#0.0").format(jsonArrayHourly.getJSONObject(i).getDouble("temp") - 273.15) + "°C";
                        String image = jsonArrayHourly.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon") + ".png";
                        String description = jsonArrayHourly.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");

                        hourlyWeathers.add(new HourlyWeather(date, image, temperature, description));
                    }
                    sevenDaysWeathers.clear();
                    for (int i = 0; i < jsonArrayDaily.length(); i++) {
                        String date = unixDateConverter(jsonArrayDaily.getJSONObject(i).getLong("dt"));
                        String minTemperature = new DecimalFormat("#0.0").format(jsonArrayDaily.getJSONObject(i).getJSONObject("temp").getDouble("min") - 273.15);
                        String maxTemperature = new DecimalFormat("#0.0").format(jsonArrayDaily.getJSONObject(i).getJSONObject("temp").getDouble("max") - 273.15);
                        String image = jsonArrayHourly.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon") + ".png";
                        String description = jsonArrayHourly.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");

                        sevenDaysWeathers.add(new SevenDaysWeather(date, minTemperature, maxTemperature, image));
                    }
                    setHourlyAdapter();
                    setSevenDaysAdapter();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {
                Toast.makeText(MainActivity.this, anError.getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setPreviousWeatherAdapter() {

        previousWeatherAdapter = new PreviousWeatherAdapter(previousWeathers, this);
        recyclerViewPrevious.setAdapter(previousWeatherAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewPrevious.setLayoutManager(layoutManager);
    }

    private void setSevenDaysAdapter() {
        sevenDaysWeatherAdapter = new SevenDaysWeatherAdapter(sevenDaysWeathers, this);
        recyclerViewSevenDays.setAdapter(sevenDaysWeatherAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewSevenDays.setLayoutManager(layoutManager);
    }

    private void setHourlyAdapter() {
        hourlyWeatherAdapter = new HourlyWeatherAdapter(hourlyWeathers, this);
        recyclerViewHourly.setAdapter(hourlyWeatherAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewHourly.setLayoutManager(layoutManager);
    }

    private void getCurrentLocationWeather(String latitude, String longitude) {
        ServerCalling.getCurrentLocationWeather(latitude, longitude, new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, "onResponse: " + response);
                    //JSONArray jsonArray = response.getJSONArray("weather");
                    textViewCity.setText(response.getString("name") + ", " + response.getJSONObject("sys").getString("country"));
                    //textViewCountry.setText(response.getJSONObject("sys").getString("country"));
                    textViewDescription.setText(response.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase());
                    textViewTemperature.setText((new DecimalFormat("#0.0").format(response.getJSONObject("main").getDouble("temp") - 273.15)) + "°C");
                    textViewFeels.setText("Feels like " + (new DecimalFormat("#0.0").format(response.getJSONObject("main").getDouble("feels_like") - 273.15)) + "°C");
                    timezone = response.getLong("timezone");
                    unixDate = response.getLong("dt");
                    textViewTime.setText(new SimpleDateFormat("h:mm a").format(new Date()));
                    textViewDate.setText(new SimpleDateFormat("EEE, d MMM").format(new Date()).toUpperCase());
                    //Toast.makeText(MainActivity.this,response.getJSONArray("weather").getJSONObject(0).getString("icon") , Toast.LENGTH_SHORT).show();
                    Picasso.get().load(Config.WEATHER_IMAGE_URL + response.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png").into(imageViewWeather);

                      } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError anError) {
                Toast.makeText(MainActivity.this, anError.getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String unixDateConverter(long unixDate) {
        SimpleDateFormat simpleDateFormatDate;
        Date date = new java.util.Date(unixDate * 1000L);
        simpleDateFormatDate = new SimpleDateFormat("EEE, d MMM");
        simpleDateFormatDate.setTimeZone(TimeZone.getDefault());//java.util.TimeZone.getTimeZone("GMT-4")
        String formattedDate = simpleDateFormatDate.format(date);
        System.out.println(formattedDate);
        return formattedDate;
    }

    private String unixTimeConverter(long unixTime) {
        SimpleDateFormat simpleDateFormatTime;
        Date date = new java.util.Date(unixTime * 1000L);
        simpleDateFormatTime = new SimpleDateFormat("h:mm a");
        simpleDateFormatTime.setTimeZone(TimeZone.getDefault());//java.util.TimeZone.getTimeZone("GMT-4")
        String formattedDate = simpleDateFormatTime.format(date);
        System.out.println(formattedDate);
        return formattedDate;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        //dialog.cancel();
                        onResume();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getCurrentLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (manager != null)
            manager.removeUpdates(locationListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}