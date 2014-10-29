package com.cj.nfcscanner.widgets;

import java.util.ArrayList;
import java.util.List;

import com.cj.nfcscanner.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * @ClassName BottomBar.java
 * @author Clame
 * 
 */
public class BottomBar extends LinearLayout implements OnClickListener {

    private static final int TAG_SCAN = 0;
    private static final int TAG_HISTORY  = 1;
    private Context          mContext;
    private int              lastButton  = -1;

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private List<View> itemList;

    /**
     * @FunName init
     * @Description initialize the layout
     * @param N/A
     * @return N/A
     * 
     */
    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.wigdet_bottom_bar, null);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));

        Button btnScan = (Button) layout.findViewById(R.id.bottombar_btn_scan);
        Button btnHistory = (Button) layout.findViewById(R.id.bottombar_btn_history);

        btnScan.setOnClickListener(this);
        btnHistory.setOnClickListener(this);

        btnScan.setTag(TAG_SCAN);
        btnHistory.setTag(TAG_HISTORY);

        itemList = new ArrayList<View>();
        itemList.add(btnScan);
        itemList.add(btnHistory);

        this.addView(layout);
    }

    @Override
    public void onClick(View v) {
        int tag = (Integer) v.getTag();
        switch (tag) {
        case TAG_SCAN:
            setNormalState(lastButton);
            setSelectedState(tag);
            break;
        case TAG_HISTORY:
            setNormalState(lastButton);
            setSelectedState(tag);
            break;
        }
    }

    /**
     * @FunName setSelectedState
     * @Description set the selected state for the selected button
     * @param index
     * @return N/A
     * 
     */
    public void setSelectedState(int index) {
        if (index != -1 && onItemChangedListener != null) {
            if (index > itemList.size()) {
                throw new RuntimeException("the value of default bar item can not bigger than string array's length");
            }
            itemList.get(index).setSelected(true);
            onItemChangedListener.onItemChanged(index);
            lastButton = index;
        }
    }

    /**
     * @FunName setNormalState
     * @Description set the normal state for the un-selected button
     * @param index
     * @return N/A
     * 
     */
    private void setNormalState(int index) {
        if (index != -1) {
            if (index > itemList.size()) {
                throw new RuntimeException("the value of default bar item can not bigger than string array's length");
            }
            itemList.get(index).setSelected(false);
        }
    }

    public interface OnItemChangedListener {
        public void onItemChanged(int index);
    }

    private OnItemChangedListener onItemChangedListener;

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.onItemChangedListener = onItemChangedListener;
    }

}
