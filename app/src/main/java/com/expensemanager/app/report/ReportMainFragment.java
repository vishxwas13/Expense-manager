package com.expensemanager.app.report;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class ReportMainFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    @BindView(R.id.report_activity_tab_layout_id) TabLayout tabLayout;
    @BindView(R.id.report_activity_view_pager_id) ViewPager viewPager;

    public static Fragment newInstance() {
        return new ReportMainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ReportFragmentAdapter adapter = new ReportFragmentAdapter(getFragmentManager());

        adapter.addFragment(ReportFragment.newInstance(ReportFragment.WEEKLY), "Weekly");
        adapter.addFragment(ReportFragment.newInstance(ReportFragment.MONTHLY), "Monthly");
        adapter.addFragment(ReportFragment.newInstance(ReportFragment.YEARLY), "Yearly");
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(ReportFragment.MONTHLY);
    }
}
