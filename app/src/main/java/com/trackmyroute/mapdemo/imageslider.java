package com.trackmyroute.mapdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;

public class imageslider extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageslider);
        ImageSlider imageSlider=findViewById(R.id.imageslider);
        ArrayList<SlideModel> slideModels=new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.nearbyschool, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.nearbyrestaurant, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.nearbyhospital, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels,ScaleTypes.FIT);

    }
}