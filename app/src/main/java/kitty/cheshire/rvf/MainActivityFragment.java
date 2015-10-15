package kitty.cheshire.rvf;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private static final long PAGE_MAX_VALUE = 50;

    private RecyclerView mRecyclerView;
    private Context mContext;
    private ArrayList<Long> mNumbersList;
    private long mLastTargetValue;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        generateNumbers(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rc_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);

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
        populateRCView();
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    private static long getFibViaDynamic(long start, long target) {
        long val1 = 0;
        long val2 = 1;
        long sum = val1 + val2;
        for (long i = start; i < target; i++) {
            sum = val1 + val2;
            val1 = val2;
            val2 = sum;
        }
        return sum;
    }

    private void generateNumbers(long startValue) {
        if (mNumbersList == null) {
            mNumbersList = new ArrayList<>();
        }

        mLastTargetValue = startValue + PAGE_MAX_VALUE;
        for (long i = startValue; i < mLastTargetValue; i++) {
            mNumbersList.add(getFibViaDynamic(startValue, i));
        }
    }

    private void populateRCView() {
        mRecyclerView.setAdapter(new ElementAdapter(mNumbersList, new RecyclerViewOnClickListener() {
            @Override
            public void onItemClick(View view, Object tag, int position) {
                Snackbar.make(view, "Clicked on : " + String.valueOf((Long) tag), Snackbar.LENGTH_SHORT)
                        .show();
            }

            @Override
            public boolean onItemLongClick(View view, Object tag, int position) {
                return false;
            }
        }));
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
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return listener.onItemLongClick(itemView, tTag, tPosition);
                }
            });
        }
    }

    private interface RecyclerViewOnClickListener {
        void onItemClick(View view, Object tag, int position);
        boolean onItemLongClick(View view, Object tag, int position);
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
