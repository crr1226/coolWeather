package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.json.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    //处理省级数据
    public static boolean handleProvinceResponse(String response){
        if(TextUtils.isEmpty(response)){
            try{
                JSONArray allPronvince = new JSONArray(response);
                //遍历所有省
                for (int i=0 ; i<allPronvince.length();i++){
                    JSONObject provinceObject = allPronvince.getJSONObject(i);
                    Province province = new Province();

                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));

                    province.save();
                }
            }catch (JSONException e){
                e.printStackTrace();

            }
            return true;
        }
        return false;
    }
    //处理市级数据
    public  static  boolean handlecityResponce(String response,int provinceCode){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                //遍历所有市
                for (int i =0;i< allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();

                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    //市所属省级
                    city.setProvinceCode(provinceCode);

                    city.save();

                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    //处理县级数据
    public static boolean handleProvinceResponse(String response , int cityCode){
        if(TextUtils.isEmpty(response)){
            try{
                JSONArray allCountries = new JSONArray(response);
                //遍历所有县
                for (int i=0 ; i<allCountries.length();i++){
                    JSONObject countryObject = allCountries.getJSONObject(i);
                    County county = new County();

                    county.setCountyName(countryObject.getString("name"));
                    county.setWeather_id(countryObject.getString("weather.id"));
                    county.setCityCode(cityCode);

                    county.save();
                }
            }catch (JSONException e){
                e.printStackTrace();

            }
            return true;
        }
        return false;
    }
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }
}
