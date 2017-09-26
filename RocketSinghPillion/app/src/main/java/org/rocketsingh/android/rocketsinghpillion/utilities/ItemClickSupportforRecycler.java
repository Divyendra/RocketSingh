package org.rocketsingh.android.rocketsinghpillion.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Divyu on 10/4/2015.
 */
public class ItemClickSupportforRecycler {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private static final int TAG = -1;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };
    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                return mOnItemLongClickListener.onItemLongClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };
    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    private ItemClickSupportforRecycler(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setTag(TAG, this);
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static ItemClickSupportforRecycler addTo(RecyclerView view) {
        ItemClickSupportforRecycler support = (ItemClickSupportforRecycler) view.getTag(TAG);
        if (support == null) {
            support = new ItemClickSupportforRecycler(view);
        }
        return support;
    }

    public static ItemClickSupportforRecycler removeFrom(RecyclerView view) {
        ItemClickSupportforRecycler support = (ItemClickSupportforRecycler) view.getTag(TAG);
        if (support != null) {
            support.detach(view);
        }
        return support;
    }

    public ItemClickSupportforRecycler setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public ItemClickSupportforRecycler setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        return this;
    }

    private void detach(RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(TAG, null);
    }

    public interface OnItemClickListener {
        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}