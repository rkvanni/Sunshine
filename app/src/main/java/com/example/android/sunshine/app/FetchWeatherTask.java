package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.android.sunshine.app.ForecastFragment.mForecastAdapter;

/**
 * Created by Renato on 25/11/2016.
 */


public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected String[] doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        int numDays = 14;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            // URI to build
            Uri.Builder BaseUriBuilder = new Uri.Builder();
            // base parameters
            BaseUriBuilder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("q",params[0])
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units","metric")
                .appendQueryParameter("cnt",Integer.toString(numDays))
                .appendQueryParameter("APPID",BuildConfig.OPEN_WEATHER_MAP_API_KEY);

            String strURL = BaseUriBuilder.build().toString();

            // Log.v(LOG_TAG, strURL);

            URL url = new URL(strURL);

            // Create the request to OpenWeatherMap, and open the connection
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


            forecastJsonStr = buffer.toString();

            // Log.v(LOG_TAG, forecastJsonStr);

            WeatherDataParser weatherParser = new WeatherDataParser();

            try {
                return weatherParser.getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch (JSONException e){
                // catch JSON parsing exception
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
            }

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
                    Log.e(LOG_TAG,"Error closing stream", e);
                }

            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);

        List<String> weekForecast = new ArrayList<>(Arrays.asList(result));

        mForecastAdapter.clear();
        mForecastAdapter.addAll(weekForecast);

    }
}
