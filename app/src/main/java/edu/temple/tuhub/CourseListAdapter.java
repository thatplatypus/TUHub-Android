package edu.temple.tuhub;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;
import edu.temple.tuhub.models.Course;

// Created by connorcrawford on 3/26/17

class CourseListAdapter extends ArrayAdapter<Course> {

    private List courses;

    CourseListAdapter(Context context, List<Course> courses) {
        super(context, android.R.layout.simple_list_item_2, courses);
        this.courses = courses;
    }

    @Override
    public int getCount() {
        return courses.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Course course = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        // Lookup view for data population
        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
        // Populate the data into the template view using the data object
        text1.setText(course != null ? course.getName() : "");
        text2.setText(course != null ? course.getTitle() : "");
        text1.setTextColor(Color.BLACK);
        text2.setTextColor(Color.DKGRAY);
        // Return the completed view to render on screen
        return convertView;
    }
}
