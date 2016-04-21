/*
 *     Popular Movies | The app allow users to discover the most popular movies playing
 *     Copyright (C) <2016>  <Alaa Elias>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.android.popularmovies.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private MoviesListAdapter moviesListAdapter;
    private final Context mContext;


    public FetchMoviesTask(Context context, MoviesListAdapter mMoviesListAdapter) {
        mContext = context;
        moviesListAdapter = mMoviesListAdapter;

        Log.i(LOG_TAG, "FetchMoviesTask");
    }

    protected void appendMoviesDataFromJsonToAdapter(String jsonString) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OPM_LIST = "results";
        final String OPM_ID = "id";
        final String OPM_TITLE = "original_title";
        final String OPM_DESCRIPTION = "overview";
        final String OPM_RATE = "vote_average";
        final String OPM_DATE = "release_date";
        final String OPM_LANG = "original_language";
        final String OPM_ADULT = "adult";
        final String OPM_IMG = "poster_path";

        JSONObject moviesJson = new JSONObject(jsonString);

        JSONArray moviesArray = moviesJson.getJSONArray(OPM_LIST);

        for(int i = 0; i < moviesArray.length(); i++) {

            // Get the JSON object representing the one movie row
            JSONObject movieRow = moviesArray.getJSONObject(i);
            HashMap<String, String> mItem = new HashMap<String, String>();

            mItem.put(MoviesListAdapter.HASH_MAP_KEY_ID, movieRow.getString(OPM_ID));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_TITLE, movieRow.getString(OPM_TITLE));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_DESCRIPTION, movieRow.getString(OPM_DESCRIPTION));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_DATE, movieRow.getString(OPM_DATE));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_RATE, movieRow.getString(OPM_RATE));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_ADULT, movieRow.getString(OPM_ADULT));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_LANG, movieRow.getString(OPM_LANG));
            mItem.put(MoviesListAdapter.HASH_MAP_KEY_IMAGE, movieRow.getString(OPM_IMG));
            moviesListAdapter.appendMovie(mItem);
            //Log.i(LOG_TAG, v);//
        }
        
        Log.i(LOG_TAG, "Count: " + String.valueOf(moviesListAdapter.getCount()));


    }

    @Override
    protected String[] doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;
        try {
            // Construct the URL for the themovieDB query
            // Possible parameters are avaiable at themovieDB API page, at
            // https://www.themoviedb.org/documentation/api
            Uri builtUri = Uri.parse(BuildConfig.THE_MOVIE_DB_API_BASE_URL +
                    BuildConfig.THE_MOVIE_DB_API_POPULAR_URL).buildUpon()
                    .appendQueryParameter(BuildConfig.THE_MOVIE_DB_API_APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY).build();

            URL url = new URL(builtUri.toString());

            Log.i(LOG_TAG, builtUri.toString());

            // Create the request to themovieDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJsonStr = buffer.toString();
            Log.i(LOG_TAG, moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            appendMoviesDataFromJsonToAdapter(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
        moviesListAdapter.notifyDataSetChanged();
    }
}