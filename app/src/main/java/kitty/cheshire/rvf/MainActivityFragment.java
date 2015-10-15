package kitty.cheshire.rvf;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    // TODO: modify this to set the target for Fibonacci generation
    private static final long FIBONACCI_TARGET_VALUE = 10000;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private ArrayList<Long> mNumbersList;
    private boolean isListGenerated;
    private ProgressDialog mProgressDialog;
    private AsyncTask<Long, Long, ArrayList<Long>> mTask;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rc_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isListGenerated) {
            generateNumbers(FIBONACCI_TARGET_VALUE, new Runnable() {
                @Override
                public void run() {
                    populateRCView();
                }
            });
        } else {
            populateRCView();
        }
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mTask != null) {
                mTask.cancel(true);
            }
            Snackbar.make(getView(), "Free memory is dangerous low", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Activity) mContext).finish();
                        }
                    })
                    .show();
        }
    }

    private static long getFibViaDynamic(long target) {
        long val1 = 0;
        long val2 = 1;
        long sum = val1 + val2;
        for (long i = 0; i < target; i++) {
            sum = val1 + val2;
            val1 = val2;
            val2 = sum;
        }
        return sum;
    }

    private void generateNumbers(long lastTargetValue, final Runnable trigger) {
        if (mContext != null) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setIndeterminate(true);
            }
        }
        mTask = new AsyncTask<Long, Long, ArrayList<Long>>() {

            @Override
            protected void onPreExecute() {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setMessage("Generating numbers ...");
                mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mTask.cancel(true);
                        ((Activity) mContext).finish();
                    }
                });
                mProgressDialog.show();
            }

            @Override
            protected ArrayList<Long> doInBackground(Long... params) {
                mNumbersList = new ArrayList<>();

                for (long i = 0; i < params[0]; i++) {
                    long number = getFibViaDynamic(i);
                    Log.i(TAG, "Number : " + String.valueOf(number));
                    mNumbersList.add(number);
                }
                return mNumbersList;
            }

            @Override
            protected void onPostExecute(ArrayList<Long> longs) {
                mProgressDialog.dismiss();
                isListGenerated = true;
                trigger.run();
            }
        }.execute(lastTargetValue);
    }

    private void populateRCView() {
        if (isListGenerated) {
            mRecyclerView.setAdapter(new ElementAdapter(mNumbersList, new RecyclerViewOnClickListener() {
                @Override
                public void onItemClick(View view, Object tag, int position) {
                    Snackbar.make(view, "Clicked on : " + String.valueOf((Long) tag), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }));
        }
    }

    private static class ElementViewHolder extends RecyclerView.ViewHolder {
        private TextView tTitle;
        private Object tTag;
        private int tPosition;

        public ElementViewHolder(final View itemView, final RecyclerViewOnClickListener listener) {
            super(itemView);
            tTitle = (TextView) itemView.findViewById(R.id.rv_view_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(itemView, tTag, tPosition);
                }
            });
        }
    }

    private interface RecyclerViewOnClickListener {
        void onItemClick(View view, Object tag, int position);
    }

    private class ElementAdapter extends RecyclerView.Adapter<ElementViewHolder> {

        private List<Long> mNumbers;
        private RecyclerViewOnClickListener mListener;

        public ElementAdapter(List<Long> numbers, RecyclerViewOnClickListener listener) {
            mNumbers = numbers;
            mListener = listener;
        }

        @Override
        public ElementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_list_element_layout, parent, false);
            return new ElementViewHolder(itemView, mListener);
        }

        @Override
        public void onBindViewHolder(ElementViewHolder holder, int position) {
            long number = mNumbers.get(position);

            holder.tTitle.setText("Number : " + String.valueOf(number));
            holder.tPosition = position;
            holder.tTag = number;
        }

        @Override
        public int getItemCount() {
            return mNumbers != null ? mNumbers.size() : 0;
        }
    }
}
