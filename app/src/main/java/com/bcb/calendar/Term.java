package com.bcb.calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created on 3/24/17.
 */

public class Term {

    private String termID;
    private String name;
    private Date startDate;
    private Date endDate;
    private List<Course> courses;

    private Term(String termID, String name, Date startDate, Date endDate, List<Course> courses) {
        this.termID = termID;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courses = courses;
    }

    static Term createTerm(JSONObject jsonObject) throws JSONException {
        String termID = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        String startDateStr = jsonObject.getString("startDate");
        String endDateStr = jsonObject.getString("endDate");

        // TODO: Parse string into date
        Date startDate = null;
        Date endDate = null;

        JSONArray sectionJSON = jsonObject.getJSONArray("sections");
        ArrayList<Course> courses = new ArrayList<>();
        for (int i = 0; i < sectionJSON.length(); i++) {
            JSONObject courseJSON = sectionJSON.getJSONObject(i);
            if (courseJSON == null)
                break;
            Course course = Course.createCourse(jsonObject, termID);
            if (course != null)
                courses.add(course);
        }

        return new Term(termID, name, startDate, endDate, courses);
    }


    public String getTermID() {
        return termID;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
