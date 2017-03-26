package com.bcb.calendar.calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bcb.calendar.Credential;
import com.bcb.calendar.NetworkManager;
import com.bcb.calendar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CourseCalendarFragment extends Fragment implements CalendarView.EventHandler {

    @BindView(R.id.calendar_view)
    CalendarView calendar;
    @BindView(R.id.calendar_scroll_contents)
    LinearLayout eventList;

    private CalendarClickListener mListener;
    private ArrayList<JSONObject> allSemesters;
    private final String termKey = "terms";
    private final String startDateKey = "startDate";
    private final String endDateKey = "endDate";
    private final String sectionsKey = "sections";
    private final String sectionTitleKey = "sectionTitle";
    private final String meetingPatternsKey = "meetingPatterns";
    private final String daysOfWeekKey = "daysOfWeek";
    private final String startTimeKey = "sisStartTimeWTz";
    private final String endTimeKey = "sisEndTimeWTz";
    private final String roomKey = "room";
    private final String buildingKey = "building";


    public CourseCalendarFragment() {
        // Required empty public constructor
    }


    public static CourseCalendarFragment newInstance() {
        CourseCalendarFragment fragment = new CourseCalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_course_calendar, container, false);
        ButterKnife.bind(this, rootView);
        calendar.setEventHandler(CourseCalendarFragment.this);

        getCourses();

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       if (context instanceof CalendarClickListener) {
            mListener = (CalendarClickListener) context;
        } else {
           throw new RuntimeException(context.toString()
                   + " must implement OnFragmentInteractionListener");
       }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof CalendarClickListener) {
            mListener = (CalendarClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDayClick(Date date) {

        Log.d("Day clicked: ", date.toString());

        JSONObject semester = getSemester(date);
        if (semester != null && allSemesters !=null) {
            populateEventList(semester, date);
        } else {
            eventList.removeAllViews();
        }

    }

    private JSONObject getSemester(Date date) {
        for (int i = 0; i < allSemesters.size(); i++) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                String stringDate = allSemesters.get(i).getString(startDateKey);
                Log.d("Start Date: ", stringDate);
                Date startDate = sdf.parse(stringDate);

                stringDate = allSemesters.get(i).getString(endDateKey);
                Date endDate = sdf.parse(stringDate);
                Log.d("End Date: ", stringDate);

                if (!date.before(startDate) && !date.after(endDate)) {
                    Log.d("Found Semester: ", String.valueOf(i));
                    return allSemesters.get(i);
                }

            } catch (JSONException | ParseException e) {
                setError("getSemester : " + e.toString());
            }
        }
        Log.d("No Semester Found: ", "null");
        return null;
    }

    private void populateEventList(JSONObject semester, Date date) {
        eventList.removeAllViews();

        try {
            JSONArray sections = semester.getJSONArray(sectionsKey);

            for(int i = 0; i<sections.length(); i++){
                JSONObject thisSection = sections.getJSONObject(i);
                Log.d("Adding Course: " , thisSection.toString());
                String title = thisSection.getString(sectionTitleKey);
                JSONArray meetingPatterns = thisSection.getJSONArray(meetingPatternsKey);
                //Traverse meetingPatterns backwards because meetings are stored by time of day latest first
                for(int j = meetingPatterns.length() - 1; j >= 0; j--) {
                    JSONObject meeting = meetingPatterns.getJSONObject(j);
                    String startTime = meeting.getString(startTimeKey);
                    String endTime = meeting.getString(endTimeKey);
                    String room = meeting.getString(roomKey);
                    String building = meeting.getString(buildingKey);

                    JSONArray days = meetingPatterns.getJSONObject(j).getJSONArray(daysOfWeekKey);
                    //Check to see if meeting is on this day. Use date.getDay() + 1 because the day index in the API starts at 1 not 0
                    if (intArrayIncludes(days, date.getDay() + 1)) {

                        EventListElement event = new EventListElement(getActivity());
                        event.setEventText(title + "\n" + building + ": " + room + "\n" + toTwelveHour(startTime) + " - " + toTwelveHour(endTime));
                        event.setOnClickListener(new EventClickListener(thisSection));
                        eventList.addView(event);
                    }
                }
            }


        } catch (JSONException e) {
            setError("populateEventList: " + e.toString());
        }

    }

    private boolean intArrayIncludes(JSONArray intArray, int thisInt){
        try {
            for (int j = 0; j < intArray.length(); j++) {

                if (intArray.getInt(j) == thisInt) {
                    return true;
                }
            }
        } catch (JSONException e){
            setError("intArrayIncludes: " + e.toString());
        }
        return false;
    }

    private String toTwelveHour(String time){
        time = time.substring(0, 5);
        try{
            String suffix;
            String hour = time.substring(0, 2);
            int timeValue = Integer.parseInt(hour);

            if(timeValue >= 13){
                timeValue = timeValue - 12;
                hour = String.valueOf(timeValue);
                suffix = " PM";
            } else {
                suffix = " AM";
            }

            return hour + time.substring(2) + suffix;
        } catch (NumberFormatException e){
            setError("toTwelveHour: " + e.toString());
            Log.d("toTwelveHour: ",  e.toString());
        }

        return null;
    }

    private void getCourses() {
        //TODO remove hardcoded credentials
        Credential credential = new Credential("tue94788", "t2theMAX");
        JSONObjectRequestListener requestListener = new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject courseInfo) {
                Log.d("Responded", "getCourses: " + courseInfo.toString());
                sortCoursesBySemester(courseInfo);
                onDayClick(new Date());
            }

            @Override
            public void onError(ANError anError) {
                if (anError.getErrorCode() != 0) {
                    // received error from server
                    // error.getErrorCode() - the error code from server
                    // error.getErrorBody() - the error body from server
                    // error.getErrorDetail() - just an error detail
                    String error = "onError errorCode : " + anError.getErrorCode() + "\n" +
                            "onError errorBody : " + anError.getErrorBody() + "\n" +
                            "onError errorDetail : " + anError.getErrorDetail();
                    setError(error);
                } else {
                    // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                    String error = "onError errorDetail : " + anError.getErrorDetail();
                    setError(error);
                }

            }
        };

        NetworkManager.SHARED.requestFromEndpointWithAuthenticator(NetworkManager.Endpoint.COURSES, "914432157", null, credential, requestListener);

    }

    private void sortCoursesBySemester(JSONObject courseInfo) {

        allSemesters = new ArrayList<>();

        try {
            JSONArray terms = courseInfo.getJSONArray(termKey);

            int i = 0;
            while (i < terms.length()) {
                Log.d("Adding term: ", terms.getJSONObject(i).toString());
                allSemesters.add(terms.getJSONObject(i));
                i++;
            }


        } catch (JSONException e) {
            setError("sortCoursesBySemester: " + e.toString());
        }

    }

    private void setError(String error) {
        TextView errorMessage = new TextView(getActivity());
        errorMessage.setText("An error occurred while retrieving course information: " + error);
        eventList.removeAllViews();
        eventList.addView(errorMessage);
    }

    private class EventClickListener implements View.OnClickListener{
        private JSONObject section;

        public EventClickListener(JSONObject section){
            this.section = section;
        }

        @Override
        public void onClick(View v) {
                mListener.showCourseDetails(section);

        }
    }

    public interface CalendarClickListener {
        void showCourseDetails(JSONObject course);
    }

}
