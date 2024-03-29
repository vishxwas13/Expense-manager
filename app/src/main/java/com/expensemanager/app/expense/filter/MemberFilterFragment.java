package com.expensemanager.app.expense.filter;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.ItemClickSupport;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MemberFilterFragment extends DialogFragment {
    private static final String TAG= MemberFilterFragment.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private Unbinder unbinder;
    private MemberFilterListener memberFilterListener;
    private ArrayList<Member> members;
    private Member member;
    private boolean isMemberFiltered;
    private MemberFilterAdapter adapter;
    private String groupId;

    @BindView(R.id.expense_member_fragment_relative_layout_id) RelativeLayout memberRelativeLayout;
    @BindView(R.id.expense_member_fragment_recycler_view_id) RecyclerView memberRecyclerView;

    public MemberFilterFragment() {}

    public static MemberFilterFragment newInstance() {
        return new MemberFilterFragment();
    }

    public void setListener(MemberFilterListener memberFilterListener) {
        this.memberFilterListener = memberFilterListener;
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
        View view = inflater.inflate(R.layout.expense_member_filter_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        members = new ArrayList<>();
        adapter = new MemberFilterAdapter(getActivity(), members, isMemberFiltered, member);

        memberRelativeLayout.setOnClickListener(v -> dismiss());

        setupRecyclerView();
        invalidateViews();
    }

    private void invalidateViews() {
        adapter.clear();
        // Add all categories
        adapter.addAll(Member.getAllAcceptedMembersByGroupId(groupId));
    }

    private void setupRecyclerView() {
        memberRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        memberRecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(memberRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                memberFilterListener.onFinishMemberFilterDialog(members.get(position));
                getDialog().dismiss();
            }
        });
    }

    public void setFilterParams(boolean isMemberFiltered, Member member) {
        this.isMemberFiltered = isMemberFiltered;
        this.member = member;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface MemberFilterListener {
        void onFinishMemberFilterDialog(Member member);
    }
}
