package com.example.admin.route;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    Button butnstart, butnreset;
    TextView time;
    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();

    public TimerFragment() {
        // Required empty public constructor
    }

    //reference
    //http://www.androidplus.org/2015/03/android-stopwatch-timer-app-tutorial-15.html

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_timer, container, false);

        butnstart = (Button) rootView.findViewById(R.id.start);

        butnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (t == 1) {
                    butnstart.setText("Pause");
                    starttime = SystemClock.uptimeMillis();
                    handler.postDelayed(updateTimer, 0);
                    t = 0;
                } else {
                    butnstart.setText("Start");
                    time.setTextColor(Color.BLUE);
                    timeSwapBuff += timeInMilliseconds;
                    handler.removeCallbacks(updateTimer);
                    t = 1;
                }}
        }
        );

        butnreset = (Button) rootView.findViewById(R.id.reset);

        butnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starttime = 0L;
                timeInMilliseconds = 0L;
                timeSwapBuff = 0L;
                updatedtime = 0L;
                t = 1;
                secs = 0;
                mins = 0;
                milliseconds = 0;
                butnstart.setText("Start");
                handler.removeCallbacks(updateTimer);
                time.setText("00:00:00");
            }
        });

        time = (TextView) rootView.findViewById(R.id.timer);

        return rootView;
    }

    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            updatedtime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            time.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            time.setTextColor(Color.RED);
            handler.postDelayed(this, 0);
        }
    };

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void passData(String time) {
            mListener.onFragmentInteraction(time.toString());

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // handle fragment arguments
        Bundle arguments = getArguments();
        if(arguments != null)
        {
            handleArguments(arguments);
        }

        // restore saved state
        if(savedInstanceState != null)
        {
            handleSavedInstanceState(savedInstanceState);
        }

        // handle intent extras
        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null)
        {
            handleExtras(extras);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // save current instance state
        super.onSaveInstanceState(outState);

        // TODO
    }

    private void handleArguments(Bundle arguments)
    {
        // TODO
    }

    private void handleSavedInstanceState(Bundle savedInstanceState)
    {
        // TODO
    }

    private void handleExtras(Bundle extras)
    {
        // TODO
    }

}
