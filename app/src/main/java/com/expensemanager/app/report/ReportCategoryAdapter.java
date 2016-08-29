package com.expensemanager.app.report;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReportCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportCategoryAdapter.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private Map<String, Integer> map;
    private Date[] startEnd;
    private Context context;

    public ReportCategoryAdapter(Context context, ArrayList<Category> categories, ArrayList<Double> amounts) {
        this.context = context;
        this.categories = categories;
        this.amounts = amounts;
        map = new HashMap<>();
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.categories.size();
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
                View view = inflater.inflate(R.layout.category_item_report, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.category_item_report, parent, false);
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
        Category category = categories.get(position);
        double amount = amounts.get(position);

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        viewHolder.colorImageView.setImageDrawable(colorDrawable);
        viewHolder.nameTextView.setText(category.getName());
        viewHolder.amountTextView.setText("$" + Helpers.formatNumToDouble(amount));

        // Item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            String categoryId = category != null ? category.getId() : null;
            ExpenseActivity.newInstance(getContext(), categoryId, startEnd);
        });
    }

    public void setStartEnd(Date[] startEnd) {
        this.startEnd = startEnd;
    }

    public void clear() {
        categories.clear();
        amounts.clear();
        map.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Expense> expenses) {
        fetchCategoriesAndAmounts(expenses);
        notifyDataSetChanged();
    }

    private void fetchCategoriesAndAmounts(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        for (Expense e : expenses) {
            String cId = e.getCategoryId();
            if (cId == null) {
                cId = NO_CATEGORY_ID;
            }
            Integer pos = map.get(cId);
            if (pos == null) {
                Category c = null;
                // Get new category
                if (cId.equals(NO_CATEGORY_ID)) {
                    // Get temp category for no category
                    c = new Category();
                    c.setColor(NO_CATEGORY_COLOR);
                    c.setName(NO_CATEGORY_ID);
                } else {
                    c = Category.getCategoryById(cId);
                }
                // Store pos of new category into map
                map.put(cId, categories.size());
                // Add new category to list
                categories.add(c);
                // Add first amount to list
                amounts.add(e.getAmount());
            } else {
                // Get current amount
                double am = amounts.get(pos);
                // Store new amount
                amounts.set(pos, am + e.getAmount());
            }
        }
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.category_item_report_color_image_view_id) CircleImageView colorImageView;
        @BindView(R.id.category_item_report_name_text_view_id) TextView nameTextView;
        @BindView(R.id.category_item_report_amount_text_view_id) TextView amountTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
