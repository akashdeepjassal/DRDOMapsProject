package com.example.shivam.drdomapsproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import az.openweatherapi.OWService;
import az.openweatherapi.listener.OWRequestListener;
import az.openweatherapi.model.OWResponse;
import az.openweatherapi.model.gson.common.Coord;
import az.openweatherapi.model.gson.five_day.ExtendedWeather;
import az.openweatherapi.model.gson.five_day.WeatherForecastElement;
import az.openweatherapi.utils.OWSupportedUnits;

public class MainActivity extends AppCompatActivity {
    OWService mOWService;
    TextView textView;
    Button view_files,go_to_map,show_route;
    List<Coord> locationModels ;
    TinyDB tinyDB;
    List<LocationModel> dao_list;
    DAOModel daoModel;
    List<Double> rain_data_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationModels = new ArrayList<>();
        rain_data_list = new ArrayList<>();
        daoModel = new DAOModel();
        dao_list = new ArrayList<>();
        show_route = (Button)findViewById(R.id.show_route);
        show_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ShowRoute.class);
                startActivity(intent);
            }
        });
        go_to_map = (Button)findViewById(R.id.go_to_map);
        view_files = (Button)findViewById(R.id.view_files_button);
        textView = (TextView)findViewById(R.id.my_text);
        mOWService = new OWService("f512468636ab7a42a5354384b04d1b6e");
        mOWService.setLanguage(Locale.ENGLISH);
        tinyDB = new TinyDB(MainActivity.this);
        mOWService.setMetricUnits(OWSupportedUnits.METRIC);
        view_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SelectFile.class);
                startActivity(intent);
            }
        });
        go_to_map.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        addCordinates();

        for ( Coord c: locationModels){
            rain_data_list.clear();
            final double lat_c = c.getLat();
            final double lon_c = c.getLon();
            mOWService.getFiveDayForecast(c, new OWRequestListener<ExtendedWeather>() {
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



                        }
                        else {
                            rain_data_list.add(0.00);
                            Log.e("My Tag: ","   0");

                        }

                    }
                    applyEquations(rain_data_list,lat_c,lon_c);


                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("5", "Five Day Forecast request failed: " + t.getMessage());
                }
            });
        }


    }
});
    }


    public void applyEquations(List<Double> rain_list,double lat,double lon){

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
        LocationModel locationModel = new LocationModel();
        locationModel.setResult(result);
        locationModel.setLatitude(lat);
        locationModel.setLongitude(lon);
        dao_list.add(locationModel);
        if(dao_list.size()==7){
            daoModel.setLocation_models(dao_list);
            tinyDB.putObject("dao",daoModel);
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            startActivity(intent);

        }


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
    public List<Coord> addCordinates(){
        locationModels.clear();
        Coord coordinate = new Coord();
        coordinate.setLat(30.00);
        coordinate.setLon(78.25);
        locationModels.add(coordinate);
        coordinate.setLat(30.25);
        coordinate.setLon(78.25);
        locationModels.add(coordinate);
        coordinate.setLat(30.50);
        coordinate.setLon(78.25);
        locationModels.add(coordinate);
        coordinate.setLat(30.75);
        coordinate.setLon(78.25);
        locationModels.add(coordinate);
        coordinate.setLat(30.75);
        coordinate.setLon(78.50);
        locationModels.add(coordinate);
        coordinate.setLat(31.00);
        coordinate.setLon(78.50);
        locationModels.add(coordinate);
        coordinate.setLat(31.00);
        coordinate.setLon(78.75);
        locationModels.add(coordinate);
       // Log.e("My List Size : ", locationModels.size()+"");
        return locationModels;
    }


}






