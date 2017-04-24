package edu.temple.tuhub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.app.Fragment;
import android.app.FragmentManager;

import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.androidnetworking.error.ANError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import edu.temple.tuhub.models.Course;
import edu.temple.tuhub.models.CourseMeeting;
import edu.temple.tuhub.models.Term;
import edu.temple.tuhub.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseFragment extends Fragment implements CourseCalendarFragment.CalendarClickListener {

    private ViewPager mViewPager;
    private static int NUM_ITEMS = 2;

    public CourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CourseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseFragment newInstance() {
        CourseFragment fragment = new CourseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (User.CURRENT != null) {
            if (User.CURRENT.getTerms() == null) {
                User.CURRENT.retrieveCourses(new User.CoursesRequestListener() {
                    @Override
                    public void onResponse(Term[] terms) {
                        if(getActivity()!=null){
                        CourseFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mViewPager != null)
                                    mViewPager.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }}

                    @Override
                    public void onError(ANError error) {
                        // TODO: Handle error
                        System.out.println(error.getErrorBody());
                    }
                });
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_course, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));

        return view;
    }

    @Override
    public void showCourseDetails(Course course) {
        //TODO implement loading the details fragment with the given course
        Log.d("Course selected:", course.getTitle());
        activity2.showCourseDetails(course);
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment[] fragments = new Fragment[NUM_ITEMS];

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            if (User.CURRENT != null && User.CURRENT.getTerms() != null)
                return NUM_ITEMS;
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (fragments[position] == null)
                        fragments[position] = CourseCalendarFragment.newInstance();
                    break;
                case 1:
                    if (fragments[position] == null)
                        fragments[position] = CourseListPagerFragment.newInstance();
                    break;
                default:
                    return null;
            }
            return fragments[position];
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Calendar";
                case 1:
                    return "List";
                default:
                    return null;
            }
        }
    }

    courseSearchHandler activity;
    showCourseDetails activity2;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity c) {
        super.onAttach(c);
        activity = (courseSearchHandler) c;
        activity2 = (showCourseDetails) c;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        activity2 = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                activity.courseSearchHandler(query,false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                return true;
            case R.id.menu_your_courses:
                activity.courseSearchHandler("", true);
                return true;
            case R.id.menu_export_courses:
                if(User.CURRENT.getTerms()!=null){
                    Term[] terms = User.CURRENT.getTerms();
                    List<Course> courses = terms[0].getCourses();
                    List<CourseMeeting> meetings;
                    for(int i = 0; i<courses.size(); i++)
                    {
                        System.out.println(courses.size()+", "+i);
                        meetings = courses.get(i).getMeetings();
                        if(meetings.size()==0){
                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, courses.get(i).getRawStartDate())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, courses.get(i).getRawStartDate())
                                    .putExtra(CalendarContract.ExtendedProperties.EVENT_LOCATION, getString(R.string.NoMeetingLocation))
                                    .putExtra(CalendarContract.ExtendedProperties.RRULE,"FREQ=WEEKLY;COUNT=15;BYDAY=MO;")
                                    .putExtra(CalendarContract.Events.TITLE, courses.get(i).getTitle());
                            //.putExtra(CalendarContract.Events.EVENT_LOCATION, courses.get(i).getMeetings().get(0).getBuildingName());
                            startActivityForResult(intent, 0);

                        }else{

                            for(int k = 0; k<meetings.size(); k++) {
                                Calendar beginCal = Calendar.getInstance();
                                Date beginDate = null;
                                try {
                                    beginDate = new SimpleDateFormat("MMMMM dd, yyyy HH:mm", Locale.US).parse(courses.get(i).getStartDate()+" "+meetings.get(k).getStartTimeWTz());
                                    meetings.get(k).getDaysString();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                beginCal.setTime(beginDate);
                                Calendar endCal = Calendar.getInstance();
                                Date endDate = null;
                                try{
                                    endDate = new SimpleDateFormat("MMMMM dd, yyyy HH:mm", Locale.US).parse(courses.get(i).getStartDate()+" "+meetings.get(k).getEndTimeWTz());
                                }catch(ParseException e){
                                    e.printStackTrace();
                                }
                                endCal.setTime(endDate);
                                int [] dw = meetings.get(k).getDaysOfWeek();
                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginCal.getTimeInMillis())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis())
                                    .putExtra(CalendarContract.ExtendedProperties.EVENT_LOCATION, meetings.get(k).getBuildingName()+", "+meetings.get(k).getRoom())
                                    .putExtra(CalendarContract.ExtendedProperties.RRULE,"FREQ=WEEKLY;COUNT="+(dw.length*15)+";BYDAY="+meetings.get(k).getDaysString())
                                    .putExtra(CalendarContract.Events.TITLE, courses.get(i).getTitle());
                            startActivityForResult(intent, 0);
                        }}
                    }
                }
                else{
                    //toast
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface courseSearchHandler {
        void courseSearchHandler(String query, boolean myCourses);
    }
    public interface showCourseDetails{
        void showCourseDetails(Course course);
    }
}

