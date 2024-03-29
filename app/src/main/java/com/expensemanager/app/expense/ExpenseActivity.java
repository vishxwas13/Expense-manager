package com.expensemanager.app.expense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.filter.CategoryFilterFragment;
import com.expensemanager.app.expense.filter.DateFilterFragment;
import com.expensemanager.app.expense.filter.MemberFilterFragment;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.Analytics;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.service.SyncExpense;
import com.twotoasters.jazzylistview.effects.SlideInEffect;
import com.twotoasters.jazzylistview.recyclerview.JazzyRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ExpenseActivity extends BaseActivity {

    private static final String TAG = ExpenseActivity.class.getSimpleName();

    public static final String CATEGORY_ID = "category_id";
    public static final String START_END_DATE = "startEnd";
    public static final String IS_CATEGORY_FILTERED = "is_category_filtered";
    public static final String CATEGORY_FRAGMENT = "Category_Fragment";
    public static final String DATE_FRAGMENT = "Date_Fragment";
    public static final String USER_FRAGMENT = "User_Fragment";

    private ArrayList<Expense> expenses;
    private Member member;
    private boolean isMemberFiltered;
    private Category category;
    private boolean isCategoryFiltered;
    private Date startDate;
    private Date endDate;
    private boolean isDateFiltered;
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;

    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.expense_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.expense_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, String categoryId) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        intent.putExtra(CATEGORY_ID, categoryId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, Date[] startEnd) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(START_END_DATE, startEnd);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, String categoryId, Date[] startEnd) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(START_END_DATE, startEnd);
        intent.putExtras(bundle);
        intent.putExtra(CATEGORY_ID, categoryId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_activity);
        ButterKnife.bind(this);

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            isDateFiltered = true;
            Date[] startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            startDate = startEnd[0];
            endDate = startEnd[1];
        }
        if (getIntent().hasExtra(CATEGORY_ID)) {
            isCategoryFiltered = true;
            String categoryId = getIntent().getStringExtra(CATEGORY_ID);
            category = Category.getCategoryById(categoryId);
        }

        expenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenses);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncExpense.getAllExpensesByGroupId(groupId);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncExpense.getAllExpensesByGroupId(groupId).continueWith(onGetExpenseFinished, Task.UI_THREAD_EXECUTOR);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);

    }

    private Continuation<Void, Void> onGetExpenseFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }

            return null;
        }
    };

    private void invalidateViews() {
        expenseAdapter.clear();

        // Check size of group members
        if (Member.getAllAcceptedMembersByGroupId(groupId).size() > 1) {
            expenseAdapter.setShowMember(true);
        } else {
            expenseAdapter.setShowMember(false);
        }

        if (isMemberFiltered && isCategoryFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndDateAndCategoryAndGroupId(member, startDate, endDate, category, groupId));
        } else if (isMemberFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndDateAndGroupId(member, startDate, endDate, groupId));
        } else if (isMemberFiltered && isCategoryFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndCategoryAndGroupId(member, category, groupId));
        } else if (isCategoryFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndCategoryAndGroupId(startDate, endDate, category, groupId));
        } else if (isMemberFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndGroupId(member, groupId));
        } else if (isCategoryFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByCategoryAndGroupId(category, groupId));
        } else if (isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndGroupId(startDate, endDate, groupId));
        } else {
            expenseAdapter.addAll(Expense.getAllExpensesByGroupId(groupId));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);
        // Add JazzyListView scroll effect
        JazzyRecyclerViewScrollListener jazzyScrollListener = new JazzyRecyclerViewScrollListener();
        recyclerView.addOnScrollListener(jazzyScrollListener);
        jazzyScrollListener.setTransitionEffect(new SlideInEffect());
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.expense));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (Member.getAllAcceptedMembersByGroupId(groupId).size() < 2) {
            Log.i(TAG, "accepted member count: " + Member.getAllAcceptedMembersByGroupId(groupId).size());
            menu.getItem(0).setVisible(false);
            menu.getItem(0).setEnabled(false);
            // You can also use something like:
            // menu.findItem(R.id.example_foobar).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_user_fragment_id:
                setupMember();
                break;
            case R.id.menu_item_category_fragment_id:
                setupCategory();
                break;
            case R.id.menu_item_date_fragment_id:
                setupDate();
                break;
        }

        return true;
    }

    private void setupMember() {
        MemberFilterFragment memberFilterFragment = MemberFilterFragment.newInstance();
        memberFilterFragment.setListener(memberFilterListener);
        memberFilterFragment.setFilterParams(isMemberFiltered, member);
        memberFilterFragment.show(getSupportFragmentManager(), USER_FRAGMENT);

        Analytics.track(getString(R.string.event_expense_date_filter_clicked));
    }

    private void setupDate() {
        DateFilterFragment dateFilterFragment = DateFilterFragment.newInstance();
        dateFilterFragment.setListener(dateFilterListener);
        dateFilterFragment.setFilterParams(startDate, endDate);
        dateFilterFragment.show(getSupportFragmentManager(), DATE_FRAGMENT);

        Analytics.track(getString(R.string.event_expense_date_filter_clicked));
    }

    private void setupCategory() {
        CategoryFilterFragment categoryFilterFragment = CategoryFilterFragment.newInstance();
        categoryFilterFragment.setListener(categoryFilterListener);
        categoryFilterFragment.setFilterParams(isCategoryFiltered, category);
        categoryFilterFragment.show(getSupportFragmentManager(), CATEGORY_FRAGMENT);
    }

    private MemberFilterFragment.MemberFilterListener memberFilterListener = new MemberFilterFragment.MemberFilterListener() {
        @Override
        public void onFinishMemberFilterDialog(Member member) {
            if (!isMemberFiltered ||
                (ExpenseActivity.this.member != null && member != null && ExpenseActivity.this.member.getId().equals(member.getId()))) {
                isMemberFiltered = !isMemberFiltered;
            }
            ExpenseActivity.this.member = member;
            invalidateViews();
        }
    };

    private DateFilterFragment.DateFilterListener dateFilterListener = new DateFilterFragment.DateFilterListener() {
        @Override
        public void onFinishDateFilterDialog(Date startDate, Date endDate) {
            isDateFiltered = startDate != null || endDate != null;
            ExpenseActivity.this.startDate = startDate;
            ExpenseActivity.this.endDate = endDate;
            invalidateViews();
        }
    };

    private CategoryFilterFragment.CategoryFilterListener categoryFilterListener = new CategoryFilterFragment.CategoryFilterListener() {
        @Override
        public void onFinishCategoryFilterDialog(Category category) {
            if (!isCategoryFiltered ||
                    ((ExpenseActivity.this.category == null && category == null) || (ExpenseActivity.this.category != null
                            && category != null && ExpenseActivity.this.category.getId().equals(category.getId())))) {
                isCategoryFiltered = !isCategoryFiltered;
            }
            ExpenseActivity.this.category = category;
            invalidateViews();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
