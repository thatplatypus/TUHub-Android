package edu.temple.tuhub;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.temple.tuhub.models.Term;
import edu.temple.tuhub.models.User;

public class CourseListPagerFragment extends Fragment {

    private static final String ARG_CURRENT_PAGE = "current-page";

    private int mCurrentPage = 0;
    private Term[] mTerms;

    public CourseListPagerFragment() {
        // Required empty public constructor
    }

    public static CourseListPagerFragment newInstance() {
        return new CourseListPagerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(ARG_CURRENT_PAGE);
        }
        if (User.CURRENT != null)
            mTerms = User.CURRENT.getTerms();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_list_pager, container, false);
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_CURRENT_PAGE, mCurrentPage);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            if (mTerms != null)
                return mTerms.length;
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            return CourseListFragment.newInstance(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (mTerms != null)
                return mTerms[position].getName();
            return null;
        }
    }
}
