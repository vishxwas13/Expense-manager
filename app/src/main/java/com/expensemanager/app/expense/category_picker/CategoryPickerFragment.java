package com.expensemanager.app.expense.category_picker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.ItemClickSupport;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryPickerFragment extends DialogFragment {
    private static final String TAG= CategoryPickerFragment.class.getSimpleName();

    private Unbinder unbinder;
    private CategoryPickerListener categoryPickerListener;
    private ArrayList<Category> categories;
    private CategoryPickerAdapter adapter;
    private String groupId;

    @BindView(R.id.expense_category_fragment_recycler_view_id) RecyclerView categoryRecyclerView;

    public CategoryPickerFragment() {}

    public static CategoryPickerFragment newInstance() {
        return new CategoryPickerFragment();
    }

    public void setListener(CategoryPickerListener categoryPickerListener) {
        this.categoryPickerListener = categoryPickerListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CategoryColorDialogStyle);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expense_category_picker_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categories = new ArrayList<>();
        adapter = new CategoryPickerAdapter(getActivity(), categories);

        setupRecyclerView();
        invalidateViews();
    }

    private void invalidateViews() {
        adapter.clear();
        // Add no category option
        adapter.add(null);
        // Add all categories
        adapter.addAll(Category.getAllCategoriesByGroupId(groupId));
    }

    private void setupRecyclerView() {
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryRecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(categoryRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                categoryPickerListener.onFinishExpenseCategoryDialog(categories.get(position));
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface CategoryPickerListener {
        void onFinishExpenseCategoryDialog(Category category);
    }
}
