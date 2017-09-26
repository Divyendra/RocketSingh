package org.rocketsingh.android.rocketsinghbiker.ui.navdrawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.rocketsingh.android.rocketsinghbiker.R;


/**
 * Created by Divyu on 5/8/2015.
 */
public class DrawerAdapter extends BaseAdapter {

    private final int mInitialPadding;
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final DrawerItems mItems = new DrawerItems();

    public DrawerAdapter(Context context) {
//        mNavMenuTitles = context.getResources().getStringArray(R.array.intruo_nav_menu);
//        mItemHasDivider = context.getResources().getStringArray(R.array.intruo_nav_menu_hasDivider);
//        mNavMenuLogoDrawables = context.getResources().obtainTypedArray(R.array.intruo_nav_menu_logodrawables);
        mInitialPadding = context.getResources().getDimensionPixelSize(R.dimen.drawer_divider_margin) * 2;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    //This would be useful in onItemClick(AdapterView<?> parent, View view, int position, long id) where we can use  parent.getItemAtPosition(position);
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DrawerViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof DrawerViewHolder)) {
            convertView = mInflater.inflate(R.layout.navdrawer_list_eachitem, parent, false);
            holder = new DrawerViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DrawerViewHolder) convertView.getTag();
        }

        DrawerItems.DrawerItem item = mItems.get(position);
        holder.txtTitle.setText(item.getTitleResId());
        //Picasso.with(mContext).load(item.getIconResId()).fit().into(holder.imgIcon);
        Glide.with(mContext).load(item.getIconResId()).into(holder.imgIcon);

        if (item.hasDivider()) {
            holder.divider.setVisibility(View.VISIBLE);
        }

        //parent is the listview object whose context is an instance of the residing activity it is in.
        if (item.isSelected(parent.getContext())) {
            holder.content.setBackgroundResource(R.color.drawer_background_selected);
        } else {
            // '0' removes the background.
            holder.content.setBackgroundResource(0);
        }

        // put space between the header and the first item
        int topPadding = (position == 0 ? mInitialPadding : 0);
        if (convertView.getPaddingTop() != topPadding) {
            convertView.setPadding(0, topPadding, 0 , 0);
        }

        return convertView;
    }

    private static class DrawerViewHolder {
        final TextView txtTitle;
        final ImageView imgIcon;
        final View divider;
        final ViewGroup content;

        DrawerViewHolder(View view) {
            txtTitle = (TextView) view.findViewById(R.id.drawer_row_title);
            imgIcon = (ImageView) view.findViewById(R.id.drawer_row_icon);
            divider = view.findViewById(R.id.drawer_row_divider);
            content = (ViewGroup) view.findViewById(R.id.drawer_row_content);
        }
    }
}
