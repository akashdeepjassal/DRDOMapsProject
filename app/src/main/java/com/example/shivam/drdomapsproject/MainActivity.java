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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import az.openweatherapi.OWService;
import az.openweatherapi.listener.OWRequestListener;
import az.openweatherapi.model.OWResponse;
import az.openweatherapi.model.gson.common.Coord;
import az.openweatherapi.model.gson.common.Main;
import az.openweatherapi.model.gson.common.Rain;
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
                        Date date = new Date(weatherForecastElement.getDt()*1000L);
                        SimpleDateFormat jdf = new SimpleDateFormat("yyMMddHHmmssZ");
                        jdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
                        String java_date = jdf.format(date).substring(0,12);
                  //      Log.e("mY Tag: Date: ",java_date);


                    }
                    else {
                        rain_data_list.add(0.00);
                        Log.e("My Tag: ","   0");

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

        List<Double> my_list = rain_list;
        Collections.reverse(my_list);
        List<Double> sub_list = my_list.subList(0,32);
        Log.e("Size of List:",sub_list.size()+" ");
        double rain_fall_sum_day_1  =  list_sum(sub_list,0,7);
        double rain_fall_sum_day_2  =  list_sum(sub_list,8,15);
        double rain_fall_sum_day_3  =  list_sum(sub_list,16,23);
        double rain_fall_sum_day_4  =  list_sum(sub_list,24,31);
        RainModel total_rain = quantify_rainfall(rain_fall_sum_day_1,rain_fall_sum_day_2,rain_fall_sum_day_3,rain_fall_sum_day_4);
        //will return rain object here, with total rain and number of hourse of event
        int result = classify_rainfall(total_rain);
        DAOModel daoModel = new DAOModel();
        daoModel.setLat(31.0439);
        daoModel.setLon(78.8418);
        daoModel.setResult(result);
        tinyDB.putString("rain","light");
        tinyDB.putDouble("lat",31.0439);
        tinyDB.putDouble("lon",78.8418);
        tinyDB.putObject("dao",daoModel);

        Intent intent = new Intent(MainActivity.this,MapsActivity.class);
        startActivity(intent);
    }


    public RainModel quantify_rainfall(double sum1, double sum2, double sum3, double sum4){
        RainModel rainModel =  new RainModel() ;
        if (sum1 > 0.5){
                //Applied Lower Limit
                rainModel.setRain_fall(sum1);
                rainModel.setHours(24);
            //Added the event of the first day, now considering the event of previous day
            if (sum2>0.5){
                //Inside Valid Zone for Day 2
                rainModel.setRain_fall(sum1+sum2);
                rainModel.setHours(48);
                if (sum3>0.5){
                    rainModel.setRain_fall(sum1+sum2+sum3);
                    rainModel.setHours(72);
                    if (sum4>0.5){
                        rainModel.setRain_fall(sum1+sum2+sum3+sum4);
                        rainModel.setHours(96);
                    }
                }

            }



            }
else {
            rainModel.setHours(0);
            rainModel.setRain_fall(0.00);
        }

    return rainModel;
    }


    public double list_sum(List<Double> list, int start_index,int end_index){
       double sum = 0.00;
        for (int i=start_index;i<=end_index;i++){
           sum+=list.get(i);
        }
        return sum;
    }
    public int classify_rainfall(RainModel rainModel){

        //Calculate the Intensity Here By Using the Object here
        double intensity = rainModel.getRain_fall()/rainModel.getHours();

        //Apply The Equation On Intensity Here and return status String according the result

        double val = 12.12354*Math.pow(rainModel.getHours(),-0.73361977);
        if (intensity<val){
            return 0;
        }
        else {
            return 1;
        }

    }


}






