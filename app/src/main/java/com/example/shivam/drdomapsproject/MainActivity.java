package com.example.shivam.drdomapsproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kwabenaberko.openweathermaplib.Lang;
import com.kwabenaberko.openweathermaplib.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.models.threehourforecast.ThreeHourForecast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import az.openweatherapi.OWService;
import az.openweatherapi.listener.OWRequestListener;
import az.openweatherapi.model.OWResponse;
import az.openweatherapi.model.gson.common.Coord;
import az.openweatherapi.model.gson.common.Main;
import az.openweatherapi.model.gson.current_day.CurrentWeather;
import az.openweatherapi.model.gson.five_day.ExtendedWeather;
import az.openweatherapi.model.gson.five_day.WeatherForecastElement;
import az.openweatherapi.utils.OWSupportedUnits;

public class MainActivity extends AppCompatActivity {
    OWService mOWService;
    TextView textView;
    TinyDB tinyDB;
    List<Double> rain_data_list;

    @Override
    //TODO : Pass the rainfall array values into equations and plot the results on the map.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rain_data_list = new ArrayList<>();
        textView = (TextView)findViewById(R.id.my_text);
        mOWService = new OWService("f512468636ab7a42a5354384b04d1b6e");
        mOWService.setLanguage(Locale.ENGLISH);
        tinyDB = new TinyDB(MainActivity.this);
        mOWService.setMetricUnits(OWSupportedUnits.METRIC);
        Coord coordinate = new Coord();
        coordinate.setLat(31.0439);
        coordinate.setLon(78.8418);


        mOWService.getFiveDayForecast(coordinate, new OWRequestListener<ExtendedWeather>() {
            @Override
            public void onResponse(OWResponse<ExtendedWeather> response) {
                ExtendedWeather extendedWeather = response.body();
                //Do something with the object here!
                for (WeatherForecastElement weatherForecastElement : extendedWeather.getList()){

                    if (weatherForecastElement.getRain().get3h()!=null){
                        rain_data_list.add(weatherForecastElement.getRain().get3h());
                        Log.e("My Tag: ","   "+weatherForecastElement.getRain().get3h());

                    }
                }

                applyEquations(rain_data_list);
                //double rain  = extendedWeather.getList().get(1).getRain().get3h();
                //Toast.makeText(MainActivity.this,"HJVHBKJ "+rain,Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("5", "Five Day Forecast request failed: " + t.getMessage());
            }
        });
    }


    public void applyEquations(List<Double> rain_list){
    //Perform the equations on the data

    tinyDB.putString("rain","light");
    tinyDB.putDouble("lat",31.0439);
    tinyDB.putDouble("lon",78.8418);
        Intent intent = new Intent(MainActivity.this,MapsActivity.class);
        startActivity(intent);
    }

}






 /*mOWService.getCurrentDayForecast(coordinate, new OWRequestListener<CurrentWeather>() {
            @Override
            public void onResponse(OWResponse<CurrentWeather> response) {
                CurrentWeather currentWeather = response.body();
                //Do something with the object here!
                double temp =  currentWeather.getMain().getTemp();
                int humidity =  currentWeather.getMain().getHumidity();

                if (currentWeather.getRain()==null){
                    Toast.makeText(MainActivity.this,"NULL VALUE",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"NOT NULL"+ currentWeather.getRain().get3h(),Toast.LENGTH_LONG).show();
                }
                Log.e("Main Activity: ",String.valueOf(temp) +" "+ String.valueOf(humidity)+"    these are values");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Main Activity: ", "Current Day Forecast request failed: " + t.getMessage());
            }
        });
*/
