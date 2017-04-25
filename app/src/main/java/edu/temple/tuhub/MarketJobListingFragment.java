package edu.temple.tuhub;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;
import com.androidnetworking.error.ANError;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import edu.temple.tuhub.models.marketplace.Job;

public class MarketJobListingFragment extends Fragment implements ImageScroller.ImageScrollerFragment{
    private ImageScroller imageScroller;
    private String username;
    private int requestCode;
    private Calendar myCalendar = Calendar.getInstance();
    AutoCompleteTextView titleInput;
    AutoCompleteTextView descriptionInput;
    AutoCompleteTextView payInput;
    AutoCompleteTextView hoursInput;
    AutoCompleteTextView startDateInput;
    AutoCompleteTextView locationInput;
    View v;

    public MarketJobListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_market_job_listing, container, false);

        imageScroller = (ImageScroller) v.findViewById(R.id.insert_job_image_scroller);
        imageScroller.verifyStoragePermissions(getActivity());
        imageScroller.setImageScrollerFragment(MarketJobListingFragment.this);
        imageScroller.setCredentialsProvider();
        if(username == null) {
            SharedPreferences pref = getActivity().getApplication().getSharedPreferences(getString(R.string.userInfo), Context.MODE_PRIVATE);
            username = pref.getString(getResources().getString(R.string.username_key), "");
        }
        titleInput = (AutoCompleteTextView) v.findViewById(R.id.editJobTitle);
        descriptionInput = (AutoCompleteTextView) v.findViewById(R.id.editJobDescription);
        payInput = (AutoCompleteTextView) v.findViewById(R.id.editJobPay);
        hoursInput = (AutoCompleteTextView) v.findViewById((R.id.editHoursPerWeek));
        startDateInput = (AutoCompleteTextView) v.findViewById(R.id.editJobStartDate);
        startDateInput.setFocusable(false);
        locationInput = (AutoCompleteTextView) v.findViewById(R.id.editJobLocation);

        startDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return v;
    }

    private void updateStartDate() {

        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        startDateInput.setText(sdf.format(myCalendar.getTime()));

    }


    @Override
    public void sendSelectImageIntent(Intent intent, int requestCode) {
        this.requestCode = requestCode;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public Activity obtainActivity(){
        return getActivity();
    }

    @Override
    public void submitListing() {
        if (validateUserInput()) {
            Job job = new Job();
            job.setTitle(titleInput.getText().toString());
            job.setDescription(descriptionInput.getText().toString());
            job.setPay(payInput.getText().toString());
            job.setHoursPerWeek (hoursInput.getText().toString());
            job.setStartDate(startDateInput.getText().toString());
            Log.d("START DATE", startDateInput.getText().toString());
            job.setLocation(locationInput.getText().toString());
            job.setOwnerId(username);
            job.setIsActive(Job.TRUE);
            Toast.makeText(getActivity(), getString(R.string.submitting), Toast.LENGTH_SHORT).show();

            job.insert(new Job.JobRequestListener() {
                @Override
                public void onResponse(Job job) {
                    Log.d("final job", job.toString());
                    if (job.getError().length() != 0) {
                        titleInput.setText(job.getError());

                    } else {

                        imageScroller.loadImagesToS3(job.getPicFileName());
                    }
                }

                @Override
                public void onError(ANError error) {
                    titleInput.setText(error.toString());
                    error.printStackTrace();

                    imageScroller.submitFailed();
                    Toast.makeText(getActivity(), getString(R.string.error_publishing), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            imageScroller.submitFailed();
        }
    }

    @Override
    public void deleteFilesFromS3(ArrayList<String> filesToDelete) {

    }

    /*
    Validates the user input and creates a job
    object out of it. Uses the Marketplace API to insert the new job. Once inserted,
     the response returns the folder name for the job's images.
     loadImagesToS3() is called with the given folder name.
     */
    private boolean validateUserInput(){
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        String pay = payInput.getText().toString();
        String hours = hoursInput.getText().toString();

        boolean valid = true;
        if(title.equals("") || title.length() == 0){
            titleInput.setError(getActivity().getString(R.string.error_field_required));
            valid = false;
        }
        if(description.length() > 2000){
            descriptionInput.setError(getActivity().getString(R.string.error_field_too_long) + String.valueOf(description.length()));
            valid = false;
        }
        if(pay.length()> 2000){
            descriptionInput.setError(getActivity().getString(R.string.error_field_too_long) + String.valueOf(description.length()));
            valid = false;
        }

        if(hours.length()> 2000){
            descriptionInput.setError(getActivity().getString(R.string.error_field_too_long) + String.valueOf(description.length()));
            valid = false;
        }
        return valid;
    }



    /*
    Handles result of image choosing intent - i.e. what to do if it is a picture
    from the camera or from the gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageScroller.onActivityResult(requestCode, resultCode, data);
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateStartDate();
        }

    };
}
