package edu.temple.tuhub.models;

import android.support.annotation.Nullable;
import android.util.Log;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * Created on 3/24/17.
 */

public class User {

    private final static String MARKETPLACE_API = "http://tuhubapi-env.us-east-1.elasticbeanstalk.com";
    private final static String SELECT_USER = "/select_user_by_id.jsp?userId=";
    private final static String INSER_USER = "/insert_user.jsp?";
    private final static String USERNAME_KEY = "TUID=";
    private final static String EMAIL_KEY = "email=";
    private final static String FIRST_NAME_KEY = "firstName=";
    private final static String LAST_NAME_KEY = "lastName=";
    private final static String PHONE_KEY = "phoneNumber=";

    public interface UserRequestListener {
        void onResponse(User user);
        void onError(ANError error);
    }

    public interface CoursesRequestListener {
        void onResponse(Term[] terms);
        void onError(ANError error);
    }

    public interface GradesRequestListener {
        void onResponse();
        void onError(ANError error);
    }

    public interface MarketplaceRequestListener {
        void onResponse(boolean isInMarketplace);
        void onError(ANError error);
    }


    @Nullable
    public static User CURRENT;

    private String username;
    private String tuID;
    private Credential credential;
    private String firstName;
    private String lastName;
    private boolean isInMarketPlace;

    @Nullable
    Term[] terms;

    private User(String username, String tuID, Credential credential) {
        this.username = username;
        this.tuID = tuID;
        this.credential = credential;
        this.firstName = "";
        this.lastName = "";
        this.isInMarketPlace = false;
    }

    public String getUsername() {
        return username;
    }

    public String getTuID() {
        return tuID;
    }

    public Credential getCredential() {
        return credential;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isInMarketPlace() {
        return isInMarketPlace;
    }

    public void setInMarketPlace(boolean inMarketPlace) {
        isInMarketPlace = inMarketPlace;
    }

    @Nullable
    public Term[] getTerms() {
        return terms;
    }

    static User createUser(JSONObject jsonObject, Credential credential) throws JSONException {
        String username = jsonObject.getString("authId");
        String tuID = jsonObject.getString("userId");

        return new User(username, tuID, credential);
    }


    public static void signInUser(String username,
                                  String password,
                                  final UserRequestListener userRequestListener) {
        System.out.println("sign in");

        final Credential credential = new Credential(username, password);
        NetworkManager.SHARED.requestFromEndpoint(NetworkManager.Endpoint.USER_INFO,
                null,
                null, credential,
                new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User user = null;
                        try {
                            // Try to initialize the user with JSON
                            user = User.createUser(response, credential);
                            // TODO: Add persistence logic
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        User.CURRENT = user;
                        userRequestListener.onResponse(user);
                    }

                    @Override
                    public void onError(ANError anError) {
                        userRequestListener.onError(anError);
                    }
                });
    }

    /**
     * Retrieves the user's grades and inserts them into their corresponding courses
     * @param courseTerms The array of terms containing the courses to insert grades into
     * @param gradesRequestListener The request listener that is called when the grades are retrieved
     */
    private void retrieveGrades(final Term[] courseTerms, final GradesRequestListener gradesRequestListener) {
        NetworkManager.SHARED.requestFromEndpoint(NetworkManager.Endpoint.GRADES,
                tuID,
                null,
                credential,
                new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray termsJSON = response.getJSONArray("terms");
                            Term[] terms = new Term[termsJSON.length()];

                            for (int i = 0; i < termsJSON.length(); i++) {
                                JSONObject termJSON = termsJSON.getJSONObject(i);
                                JSONArray coursesJSON = termJSON.getJSONArray("sections");
                                for (int j = 0; j < coursesJSON.length(); j++) {
                                    JSONObject courseJSON = coursesJSON.getJSONObject(j);

                                    // Find the corresponding course
                                    Course course = null;
                                    for (Term t: courseTerms) {
                                        if (t.getTermID().equals(termJSON.getString("id"))) {
                                            for (Course c: t.getCourses()) {
                                                if (c.getSectionID().equals(courseJSON.getString("sectionId"))) {
                                                    course = c;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    // Unable to find the corresponding course, skip grade creation
                                    if (course == null)
                                        continue;

                                    // Create the grade objects
                                    JSONArray gradesJSON = courseJSON.getJSONArray("grades");
                                    List<Grade> grades = new ArrayList<>(0);
                                    for (int k = 0; k < gradesJSON.length(); k++) {
                                        Grade grade = Grade.createGrade(gradesJSON.getJSONObject(k));
                                        if (grade != null)
                                            grades.add(grade);
                                    }

                                    // Set the course's grades to the created grades
                                    course.grades = grades;
                                }
                            }

                            gradesRequestListener.onResponse();
                        } catch (JSONException e) {
                            // TODO: Handle error
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        gradesRequestListener.onError(anError);
                    }
                });

    }

    public void retrieveCourses(final CoursesRequestListener coursesRequestListener) {
        NetworkManager.SHARED.requestFromEndpoint(NetworkManager.Endpoint.COURSES,
                tuID,
                null,
                credential,
                new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray termsJSON = response.getJSONArray("terms");
                            final Term[] terms = new Term[termsJSON.length()];
                            for (int i = 0; i < termsJSON.length(); i++) {
                                Term term = Term.createTerm(termsJSON.getJSONObject(i));
                                if (term != null)
                                    terms[i] = term;
                            }

                            retrieveGrades(terms, new GradesRequestListener() {
                                @Override
                                public void onResponse() {
                                    User.this.terms = terms;
                                    coursesRequestListener.onResponse(terms);
                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        coursesRequestListener.onError(anError);
                    }
                });
    }

    /**
     * Retrieves the user's name
     * @param gradesRequestListener The request listener that is called when the name is retrieved
     */
    public void retrieveName(final GradesRequestListener gradesRequestListener) {
        final User user = this;
        NetworkManager.SHARED.requestFromEndpoint(NetworkManager.Endpoint.GRADES,
                tuID,
                null,
                credential,
                new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject student = response.getJSONObject("student");
                            String name = student.getString("name");

                            try{
                                String[] splitName = name.split("\\s+");
                                user.firstName = splitName[0];
                                user.lastName = splitName[splitName.length-1];

                            } catch(PatternSyntaxException e){
                                ANError error = new ANError();
                                error.setErrorBody(e.toString());
                                gradesRequestListener.onError(error);
                            }

                            gradesRequestListener.onResponse();
                        } catch (JSONException e) {

                            ANError error = new ANError();
                            error.setErrorBody(e.toString());
                            gradesRequestListener.onError(error);
                            e.printStackTrace();
                        }

                    }


                    @Override
                    public void onError(ANError anError) {
                        gradesRequestListener.onError(anError);
                    }
                });
    }

    /*
    Checks to see if given username is registered in marketplace database. Calls marketplaceRequestListener(true) if a row is found,
    calls marketplaceRequestListener(false) if no rows are found
     */
    public static void isInMarketplace(final String username, final MarketplaceRequestListener marketplaceRequestListener) {
        String url = MARKETPLACE_API + SELECT_USER + username;
        NetworkManager.SHARED.requestFromUrl(url,
                null,
                null,
                null,
                new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray userArray = response.getJSONArray("userList");
                            if(userArray.length() == 0){
                                marketplaceRequestListener.onResponse(false);
                            } else {
                                marketplaceRequestListener.onResponse(true);
                            }

                        } catch (JSONException e) {

                            ANError error = new ANError();
                            error.setErrorBody(e.toString());
                            marketplaceRequestListener.onError(error);
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        marketplaceRequestListener.onError(anError);
                    }
                });
    }

    /*
    REQUIRED: This method uses CURRENT, so a user must be logged in to have this method called successfully
    Given username and tuid, a call is made to user.retrieveName() to get the first and last name of the user.
    This information is then used to insert the new user into the marketplace database user table
    MarketplaceRequestListener returns true if successful insert, false otherwise
     */
    public static void addToMarketplace(final String username, final String tuid, final String phoneNumber, final MarketplaceRequestListener marketplaceRequestListener) {
        final StringBuffer url = new StringBuffer(MARKETPLACE_API);
        url.append(INSER_USER);
        url.append(USERNAME_KEY);
        url.append(username);
        if(phoneNumber != null){
            url.append("&");
            url.append(PHONE_KEY);
            url.append(phoneNumber);
        }
        url.append("&");
        url.append(EMAIL_KEY);
        url.append(username);
        url.append("@temple.edu");

        final User user = new User(username, tuid, CURRENT.getCredential());
        user.retrieveName(new GradesRequestListener() {
            @Override
            public void onResponse() {
                url.append("&");
                url.append(FIRST_NAME_KEY);
                url.append(user.getFirstName());
                url.append("&");
                url.append(LAST_NAME_KEY);
                url.append(user.getLastName());

                NetworkManager.SHARED.requestFromUrl(url.toString(),
                        null,
                        null,
                        null,
                        new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String error = response.getString("error");
                                    if(error.length() == 0){
                                        marketplaceRequestListener.onResponse(true);
                                    } else {
                                        ANError anError = new ANError();
                                        anError.setErrorBody(error);
                                        marketplaceRequestListener.onError(anError);
                                    }

                                } catch (JSONException e) {

                                    ANError error = new ANError();
                                    error.setErrorBody(e.toString());
                                    marketplaceRequestListener.onError(error);
                                    e.printStackTrace();
                                }

                            }


                            @Override
                            public void onError(ANError anError) {
                                marketplaceRequestListener.onError(anError);
                            }
                        });
            }

            @Override
            public void onError(ANError error) {
                marketplaceRequestListener.onError(error);
            }
        });
    }

}
