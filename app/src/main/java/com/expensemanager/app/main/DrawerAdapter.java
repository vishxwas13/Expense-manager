package com.expensemanager.app.main;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.DrawerItem;
import com.expensemanager.app.models.DrawerSubItem;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_MENU = 1;
    public final static int TYPE_SUBMENU = 2;
    public final static int TYPE_DIVIDER = 3;

    private Context context;
    private ArrayList<DrawerItem> drawerMenuList;
    private ArrayList<DrawerSubItem> drawerSubMenuList;
    private User user;
    private DrawerViewHolder headerHolder;

    private OnItemSelecteListener mListener;

    public DrawerAdapter(Context context, ArrayList<DrawerItem> drawerMenuList, ArrayList<DrawerSubItem> drawerSubMenuList, User user) {
        this.context = context;
        this.drawerMenuList = drawerMenuList;
        this.drawerSubMenuList = drawerSubMenuList;
        this.user = user;
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == TYPE_HEADER){
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
        }else if (viewType == TYPE_MENU){
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
        } else if (viewType == TYPE_SUBMENU) {
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_subitem, parent, false);
        } else {
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_item_divider, parent, false);
        }

        return new DrawerViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        int type = getItemViewType(position);
        if(type == TYPE_HEADER) {
            headerHolder = holder;
            loadUser(user);

            Typeface font= Typeface.createFromAsset(context.getAssets(),
                "fonts/materialdrawerfont-font-v5.0.0.ttf");
            holder.groupSwitcherTextView.setTypeface(font);
            holder.groupSwitcherTextView.setText("\uE5C7");

            holder.groupSwitcherTextView.clearAnimation();
            ViewCompat.animate(holder.groupSwitcherTextView).rotation(-180).start();

            holder.accountPhotoImageView.setOnClickListener(v -> {
                ProfileActivity.newInstance(context, null);
            });
        } else if (type == TYPE_MENU) {
            holder.titleTextView.setText(drawerMenuList.get(position - 1).getTitle());
            holder.iconImageView.setImageResource(drawerMenuList.get(position - 1).getIcon());
        } else if (type == TYPE_SUBMENU) {
            holder.titleTextView.setText(drawerSubMenuList.get(position - drawerMenuList.size() - 2).getTitle());
        }
    }

    public void loadUser(User user) {
        if (user != null) {
            Helpers.loadProfilePhoto(headerHolder.accountPhotoImageView, user.getPhotoUrl());
            headerHolder.accountNameTextView.setText(user.getFullname());
            headerHolder.accountEmailTextView.setText(user.getEmail());

            String fullname = user.getFullname();

            if (fullname != null && !fullname.isEmpty()) {
                headerHolder.accountNameTextView.setText(fullname);
            } else {
                headerHolder.accountNameTextView.setText(context.getString(R.string.app_name));
            }
        }
    }

    @Override
    public int getItemCount() {
        return drawerMenuList.size() + drawerSubMenuList.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position <= drawerMenuList.size()) {
            return TYPE_MENU;
        } else if (position == drawerMenuList.size() + 1) {
            return TYPE_DIVIDER;
        } else {
            return TYPE_SUBMENU;
        }
    }

    public void add(DrawerItem drawerItem) {
        drawerMenuList.add(drawerItem);
        notifyItemChanged(drawerMenuList.size());
    }

    public void add(DrawerSubItem drawerSubItem) {
        drawerSubMenuList.add(drawerSubItem);
        notifyItemChanged(drawerMenuList.size() + 1);
    }

    class DrawerViewHolder extends RecyclerView.ViewHolder{
        // DrawerHeader
        ImageView accountPhotoImageView;
        TextView accountNameTextView;
        TextView accountEmailTextView;
        TextView groupSwitcherTextView;

        // DrawerItem
        TextView titleTextView;
        ImageView iconImageView;

        public DrawerViewHolder(View itemView, int viewType) {
            super(itemView);

            if(viewType == TYPE_HEADER){
                accountPhotoImageView = (ImageView) itemView.findViewById(R.id.drawer_account_header_acount_photo_id);
                accountNameTextView = (TextView) itemView.findViewById(R.id.drawer_header_name_id);
                accountEmailTextView = (TextView) itemView.findViewById(R.id.drawer_header_email_id);
                groupSwitcherTextView = (TextView) itemView.findViewById(R.id.drawer_header_group_switcher_text_view_id);
            } else if(viewType == TYPE_MENU){
                titleTextView = (TextView) itemView.findViewById(R.id.drawer_name_text_view_id);
                iconImageView = (ImageView) itemView.findViewById(R.id.drawer_icon_image_view_id);
            } else if (viewType == TYPE_SUBMENU) {
                titleTextView = (TextView) itemView.findViewById(R.id.drawer_name_text_view_id);
            }

            if (viewType == TYPE_DIVIDER) {
                return;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemSelected(view, getAdapterPosition());

                }
            });
        }

    }

    public void setOnItemClickLister(OnItemSelecteListener mListener) {
        this.mListener = mListener;
    }

    public interface OnItemSelecteListener{
        public void onItemSelected(View v, int position);
    }

}