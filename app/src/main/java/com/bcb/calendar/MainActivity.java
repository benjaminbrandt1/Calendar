package com.bcb.calendar;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.bcb.calendar.calendar.CourseCalendarFragment;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements CourseCalendarFragment.CalendarClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.courses:
                        fragment = (Fragment)CourseCalendarFragment.newInstance();
                        getFragmentManager().beginTransaction().replace(R.id.fragment_frame, fragment).commit();
                        break;
                    case R.id.marketplace:
                        // Load appropriate fragment
                        break;
                    case R.id.maps:
                        // Load appropriate fragment
                        break;
                    case R.id.news:
                        // Load appropriate fragment
                        break;
                    case R.id.account:
                        // Load appropriate fragment
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void showCourseDetails(JSONObject course) {
        try {
            Toast.makeText(this, course.getString("sectionTitle"), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
