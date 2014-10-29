package com.cj.nfcscanner.fragments;

import java.util.ArrayList;

import com.cj.nfcscanner.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @ClassName FragmentMine.java
 * @author Clame
 * 
 */
public class FragmentScan extends Fragment {

	private TextView tv_scan_count;
	private Button btn_exe;
	private TextView tv_scan_current;
	private boolean isExist = false;

	public static String SCAN_STATUS = "STATUS";
	public static String SCAN_ID = "ID";
	public static String SCAN_ID_LIST = "ID_LIST";

	OnBtnClickListener mCallback;

	public FragmentScan() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		// get the showType
		String status = "";
		String currentID = "";
		ArrayList<String> idList = new ArrayList<String>();
		Bundle args = getArguments();
		if (args != null) {
			status = args.getString(SCAN_STATUS);
			currentID = args.getString(SCAN_ID);
			idList = args.getStringArrayList(SCAN_ID_LIST);
		}

		LayoutInflater myInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = myInflater.inflate(R.layout.fragment_scan, container,
				false);

		btn_exe = (Button) layout.findViewById(R.id.btn_scan_execute);
		btn_exe.setText(status);
		setBtnStatus();
		btn_exe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setBtnStatus();
			}
		});

		tv_scan_current = (TextView) layout.findViewById(R.id.tv_scan_current);
		tv_scan_current.setText(currentID);
		if (currentID.indexOf(getResources().getString(
				R.string.scan_text_current_exist)) != -1) {
			tv_scan_current.setBackgroundColor(getResources().getColor(
					R.color.content_text_red));
			isExist = true;
		}

		tv_scan_count = (TextView) layout.findViewById(R.id.tv_scan_count);
		tv_scan_count.setText(String.valueOf(idList.size()));

		for (int i = 0; i < idList.size(); i++) {
			int itemID = getResources().getIdentifier(
					"tv_scan_history" + String.valueOf(i + 1), "id",
					"com.cj.nfcscanner");
			TextView tvList = (TextView) layout.findViewById(itemID);
			tvList.setText(idList.get(idList.size() - i - 1));
			if (isExist && currentID.indexOf(idList.get(idList.size() - i - 1)) != -1) {
				tvList.setTextColor(getResources().getColor(
						R.color.content_text_white));
				tvList.setBackgroundColor(getResources().getColor(
						R.color.content_text_red));
			}
			if (i == 4) {
				break;
			}
		}

		return layout;
	}

	private void setBtnStatus() {
		mCallback.onUpdateStatus(btn_exe.getText().toString());
		if (btn_exe.getText().equals("")) {
			btn_exe.setText(getResources().getString(
					R.string.scan_text_title_button_1));
			btn_exe.setBackgroundColor(getResources().getColor(
					R.color.content_text_green));
		} else if (btn_exe.getText().equals(
				getResources().getString(R.string.scan_text_title_button_1))) {
			btn_exe.setText(getResources().getString(
					R.string.scan_text_title_button_2));
			btn_exe.setBackgroundColor(getResources().getColor(
					R.color.content_text_red));
		} else if (btn_exe.getText().equals(
				getResources().getString(R.string.scan_text_title_button_2))) {
			btn_exe.setText(getResources().getString(
					R.string.scan_text_title_button_3));
			btn_exe.setBackgroundColor(getResources().getColor(
					R.color.content_text_blue));
		} else if (btn_exe.getText().equals(
				getResources().getString(R.string.scan_text_title_button_3))) {
			btn_exe.setText(getResources().getString(
					R.string.scan_text_title_button_2));
			btn_exe.setBackgroundColor(getResources().getColor(
					R.color.content_text_red));
		}
	}

	// Container Activity must implement this interface
	public interface OnBtnClickListener {
		public void onUpdateStatus(String pStatus);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnBtnClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}
}
