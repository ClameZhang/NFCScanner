package com.cj.nfcscanner.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cj.nfcscanner.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * @ClassName FragmentMine.java
 * @author Clame
 * 
 */
public class FragmentHistory extends Fragment {
	private static final int ITEM_DELETE = 1;
	private static final int ITEM_CANCEL = 2;

	private TextView tv_history_count;
	private Button btn_history_save;
	private EditText et_history_search;
	private ListView lv_history_list;

	private ArrayList<String> idList;

	OnItemDeleteListener mCallback;

	public FragmentHistory() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		idList = new ArrayList<String>();
		Bundle args = getArguments();
		if (args != null) {
			idList = args.getStringArrayList(FragmentScan.SCAN_ID_LIST);
		}

		LayoutInflater myInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = myInflater.inflate(R.layout.fragment_history, container,
				false);

		tv_history_count = (TextView) layout
				.findViewById(R.id.tv_history_count);
		if (idList != null) {
			tv_history_count.setText(String.valueOf(idList.size()));
		}

		btn_history_save = (Button) layout.findViewById(R.id.btn_history_save);
		btn_history_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCallback.onShowSaveDialog();
			}
		});

		et_history_search = (EditText) layout
				.findViewById(R.id.et_history_search);
		et_history_search.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String filterStr = null;
				if (et_history_search.getText().toString().trim().length() > 0) {
					filterStr = et_history_search.getText().toString().trim();
				}
				showData(filterStr);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		lv_history_list = (ListView) layout.findViewById(R.id.lv_history_list);
		registerForContextMenu(lv_history_list);
		showData(null);

		return layout;
	}

	private void showData(String filterStr) {
		ArrayList<String> idListTmp = new ArrayList<String>();
		idListTmp = idList;

		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < idListTmp.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("seq", String.valueOf(i + 1) + ".");
			map.put("id", idList.get(i));

			if (filterStr != null) {
				if (idList.get(i).indexOf(filterStr) != -1) {
					dataList.add(map);
				}
			} else {
				dataList.add(map);
			}
		}

		SimpleAdapter adapter = buildListAdapter(this.getActivity(), dataList);
		lv_history_list.setAdapter(adapter);
	}

	public SimpleAdapter buildListAdapter(Context context,
			List<Map<String, String>> data) {
		SimpleAdapter adapter = new SimpleAdapter(context, data,
				R.layout.listview_history, new String[] { "seq", "id" },
				new int[] { R.id.lv_item_seq, R.id.lv_item_id });
		return adapter;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(getResources().getString(
				R.string.history_list_operate_title));
		menu.add(0, ITEM_DELETE, 0,
				getResources().getString(R.string.history_list_operate_delete));
		menu.add(0, ITEM_CANCEL, 1,
				getResources().getString(R.string.history_list_operate_cancel));
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		String id = ((TextView) info.targetView.findViewById(R.id.lv_item_id))
				.getText().toString();

		switch (item.getItemId()) {
		case ITEM_DELETE:
			idList.remove(idList.indexOf(id));
			tv_history_count.setText(String.valueOf(idList.size()));
			mCallback.onUpdateList(idList);
			break;
		case ITEM_CANCEL:
		default:
			break;
		}

		String filterStr = null;
		if (et_history_search.getText().toString().trim().length() > 0) {
			filterStr = et_history_search.getText().toString().trim();
		}
		showData(filterStr);
		return false;
	}

	// Container Activity must implement this interface
	public interface OnItemDeleteListener {
		public void onUpdateList(ArrayList<String> newList);
		public void onShowSaveDialog();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnItemDeleteListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}
}
