package com.trackmyroute.mapdemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;


public class ImagesliderFragment extends Fragment {

    private  ImageSlider imageSlider;
    ImageView img1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_imageslider, container, false);
        imageSlider=view.findViewById(R.id.imageslider);
        img1=view.findViewById(R.id.imageView2);
        Glide.with(this)
                .asGif()
                .override(800, 800) // Load a lower resolution version of the GIF
                .load(R.drawable.imageslidergif)
                .into(img1);
        ArrayList<SlideModel> slideModels=new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.nearbyschool, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.nearbyrestaurant, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.nearbyhospital, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels,ScaleTypes.FIT);

        return view;

    }
}