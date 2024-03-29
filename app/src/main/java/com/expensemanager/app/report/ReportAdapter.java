package com.expensemanager.app.report;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/24/16.
 */

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportAdapter.class.getSimpleName();

    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Date[]> dates;
    private Context context;
    private int requestCode;

    public ReportAdapter(Context context, ArrayList<Date[]> dates, int requestCode) {
        this.context = context;
        this.dates = dates;
        this.requestCode = requestCode;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.report_item_default, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.report_item_default, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        Date[] startEnd = dates.get(position);

        String name = null;
        switch(requestCode) {
            case WEEKLY:
                name = Helpers.getWeekStartEndString(startEnd[0]);
                break;
            case MONTHLY:
                name = Helpers.getMonthStringFromDate(startEnd[0]);
                break;
            case YEARLY:
                name = Helpers.getYearStringFromDate(startEnd[0]);
                break;
        }

        if (name == null) {
            return;
        }

        viewHolder.nameTextView.setText(name);

        viewHolder.itemView.setOnClickListener(v -> {
            ReportDetailActivity.newInstance(context, startEnd, requestCode);
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void clear() {
        dates.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Date[]> dates) {
        if (dates == null) {
            return;
        }
        this.dates.addAll(dates);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.report_item_default_name_text_view_id) TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
