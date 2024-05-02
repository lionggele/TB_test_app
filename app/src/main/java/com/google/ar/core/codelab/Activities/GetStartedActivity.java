package com.google.ar.core.codelab.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.ar.core.codelab.depth.R;
import com.google.ar.core.codelab.fragment.GetStartFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class GetStartedActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout pageIndicatorLayout;
    private final List<ImageView> dots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getstartedpage);

        viewPager = findViewById(R.id.viewPager);
        pageIndicatorLayout = findViewById(R.id.pageIndicator);

        List<String> pageTexts = Arrays.asList("introduction about the App", "Homescreen page", "Result page", "Informationpage","Login in page");
        viewPager.setAdapter(new ScreenSlidePagerAdapter(this, pageTexts));

        setupPageIndicators(pageTexts.size());
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < dots.size(); i++) {
                    dots.get(i).setImageResource(i == position ? R.drawable.indicator_dot_selected : R.drawable.indicator_dot);
                }

                if (position == pageTexts.size() - 1) {
                    viewPager.setUserInputEnabled(false);
                    viewPager.setOnTouchListener((v, event) -> {
                        // Start the DepthColabActivity when the user tries to swipe past the fourth page
                        Intent intent = new Intent(GetStartedActivity.this,  LoginRegisterActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    });
                } else {
                    viewPager.setUserInputEnabled(true);
                    viewPager.setOnTouchListener(null); //
                    //Toast.makeText(GetStartedActivity.this, "Page", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setupPageIndicators(int count) {
        int dotSize = getResources().getDimensionPixelSize(R.dimen.dot_size);
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(20, 0, 20, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.indicator_dot);
            dots.add(dot);
            pageIndicatorLayout.addView(dot);
        }
        if (!dots.isEmpty()) {
            dots.get(0).setImageResource(R.drawable.indicator_dot_selected);
        }
    }



    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private final List<String> texts;

        ScreenSlidePagerAdapter(FragmentActivity fa, List<String> texts) {
            super(fa);
            this.texts = texts;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return GetStartFragment.newInstance(texts.get(position));
        }

        @Override
        public int getItemCount() {
            return texts.size();
        }
    }
}


