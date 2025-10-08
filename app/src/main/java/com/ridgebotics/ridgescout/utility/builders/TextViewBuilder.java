package com.ridgebotics.ridgescout.utility.builders;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TextViewBuilder {
    public TextView tv;
    public TextViewBuilder(Context c) {
        tv = new TextView(c);
    }

    public TextViewBuilder(Context c, String str) {
        tv = new TextView(c);
        tv.setText(str);
    }

    public TextViewBuilder size(float size) {
        tv.setTextSize(size);
        return this;
    }

    public TextViewBuilder text(String str) {
        tv.setText(str);
        return this;
    }

    public TextViewBuilder padding(int borders) {
        tv.setPadding(borders,borders,borders,borders);
        return this;
    }

    public TextViewBuilder padding(int horisontal, int vertical) {
        tv.setPadding(horisontal,vertical,horisontal,vertical);
        return this;
    }

    public TextViewBuilder padding(int left, int right, int top, int bottom) {
        tv.setPadding(left,top,right,bottom);
        return this;
    }

    public TextViewBuilder align_left() {
        tv.setGravity(Gravity.START);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        return this;
    }

    public TextViewBuilder align_center() {
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        );
//        params.gravity = Gravity.CENTER;
//        tv.setLayoutParams(params);
//
//
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return this;
    }



//    public TextViewBuilder center_xy() {
//        TableRow.LayoutParams params = new TableRow.LayoutParams();
//        params.gravity = Gravity.CENTER;
//        tv.setLayoutParams(params);
//        return this;
//    }

    public TextViewBuilder align_right() {
        tv.setGravity(Gravity.END);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        return this;
    }

    public TextViewBuilder layout_wrap_wrap() {
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return this;
    }

    public TextViewBuilder layout_wrap_match() {
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return this;
    }

    public TextViewBuilder layout_match_wrap() {
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return this;
    }

    public TextViewBuilder layout_match_match() {
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return this;
    }

    public TextViewBuilder h1() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline1);
        return this;
    }
    public TextViewBuilder h2() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline2);
        return this;
    }
    public TextViewBuilder h3() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline3);
        return this;
    }
    public TextViewBuilder h4() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline4);
        return this;
    }
    public TextViewBuilder h5() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline5);
        return this;
    }
    public TextViewBuilder h6() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6);
        return this;
    }

    public TextViewBuilder sub1() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle1);
        return this;
    }
    public TextViewBuilder sub2() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Subtitle2);
        return this;
    }

    public TextViewBuilder body1() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1);
        return this;
    }
    public TextViewBuilder body2() {
        tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2);
        return this;
    }


    public TextView build() {
        return tv;
    }
}
