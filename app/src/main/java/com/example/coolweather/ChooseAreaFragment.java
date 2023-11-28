package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int currentLevel; //当前的访问状态，对应与LEVEL_PROVINCE.....


    private TextView titleText;
    private Button back_btn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> datalist = new ArrayList<>();
    private ProgressDialog progressDialog;


    //当前的省
    private Province currentProvince;
    private City currentCity;
    //所有的省
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);

        //获取控件
        titleText = view.findViewById(R.id.title_text);
        back_btn = view.findViewById(R.id.back_btn);
        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event.getTargetState()==Lifecycle.State.CREATED){
                    queryProvince();
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if(currentLevel==LEVEL_PROVINCE){
                                currentProvince=provinceList.get(i);
                                queryCity();
                            }else if(currentLevel==LEVEL_CITY){
                                currentCity=cityList.get(i);
                                queryCounty();
                            }

                        }
                    });
                    back_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(currentLevel==LEVEL_CITY){
                                queryProvince();
                            }
                            else if(currentLevel==LEVEL_COUNTY){
                                queryCity();
                            }
                        }
                    });
                    requireActivity().getLifecycle().removeObserver(this);
                }
            }
        });
    }

    //从服务器获取数据
    public void queryFromService(String address, final String type) {
        showProgressDialog();
        //调用函数。访问服务器
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;

                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText, currentProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText, currentCity.getCityCode());
                }

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });

                }
            }
        });
    }
        /*
        1，读取所有信息，并显示在UI上
        2.如果数据库有信息，则读取数据库
        3.如果数据库没用信息，则先连接服务器读取数据并储存，再读取数据库
         */
        public void queryProvince() {
            titleText.setText("中国");
            back_btn.setVisibility(View.GONE);
            //
            provinceList = LitePal.findAll(Province.class);
            //
            if (provinceList.size() > 0) {
                datalist.clear();
                for (Province province : provinceList) {
                    datalist.add(province.getProvinceName());
                }
                //页面显示数据
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_PROVINCE;
            }else {
                String address = "http://guolin.tech/api/china";
                queryFromService(address,"province");
            }
        }
        public void queryCity(){
            titleText.setText(currentProvince.getProvinceName());
            back_btn.setVisibility(View.VISIBLE);
            LitePal.where("provinceCode=?",String.valueOf(currentProvince.getProvinceCode())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            String address="http://guolin.tech/api/china/"+currentProvince.getProvinceCode();
            queryFromService(address,"city");
        }
        }
        public void queryCounty(){
            titleText.setText(currentCity.getCityName());
            back_btn.setVisibility(View.VISIBLE);
            countyList=LitePal.where("cityCode=?",String.valueOf(currentCity.getCityCode())).find(County.class);
            if(countyList.size()>0){
                datalist.clear();
                for (County county:countyList){
                    datalist.add(county.getCountyName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel=LEVEL_COUNTY;
            }else {
               String address="http://guolin.tech/api/china/"+currentProvince.getProvinceCode()+"/"+currentCity.getCityCode();
               queryFromService(address,"county");
            }

        }
        private void showProgressDialog(){

            if(progressDialog==null){
                progressDialog=new ProgressDialog(getActivity());
                progressDialog.setMessage("正在加载中...");
                progressDialog.setCanceledOnTouchOutside(false);
            }
            progressDialog.show();
        }
        private  void closeProgressDialog(){
            if(progressDialog!=null){
                progressDialog.dismiss();
            }
        }
    }
