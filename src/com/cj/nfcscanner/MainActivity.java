package com.cj.nfcscanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;

import com.cj.nfcscanner.fragments.FragmentHistory;
import com.cj.nfcscanner.fragments.FragmentScan;
import com.cj.nfcscanner.widgets.BottomBar;
import com.cj.nfcscanner.widgets.BottomBar.OnItemChangedListener;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.EditText;

public class MainActivity extends FragmentActivity implements
		FragmentScan.OnBtnClickListener, FragmentHistory.OnItemDeleteListener {
	// the flag of show register view or not
	private NfcAdapter nfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String mStatus;
	private int mCurrentFG;
	private String mCurrentID;
	private ArrayList<String> mIDList;

	private final static int BTN_ITEM_SCAN = 0;
	private final static int BTN_ITEM_HISTORY = 1;

	private final String SDCARD_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/";
	private String APP_PATH;
	private String TMP_DATA_FILE_PATH;
	private String TMP_DATA_FILE_NAME;
	private String DATA_FILE_PATH;

	private boolean envErrorFlag = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		APP_PATH = SDCARD_PATH + getResources().getString(R.string.app_name)
				+ "/";
		TMP_DATA_FILE_PATH = APP_PATH + "tmp/";
		TMP_DATA_FILE_NAME = "tmp.txt";
		DATA_FILE_PATH = APP_PATH + "data/";

		// declare the bottombar, and add the ItemChangedListener
		final BottomBar bottomBar = (BottomBar) findViewById(R.id.main_bottom_bar);
		bottomBar.setOnItemChangedListener(new OnItemChangedListener() {
			@Override
			public void onItemChanged(int index) {
				mCurrentFG = index;
				showDetails();
			}
		});

		// initialize the nfc related settings
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);

		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

		mFilters = new IntentFilter[] { ndef, };

		mTechLists = new String[][] { new String[] { MifareUltralight.class
				.getName() } };

		// set the scan as the default fragment
		mCurrentFG = BTN_ITEM_SCAN;
		mIDList = new ArrayList<String>();
		if (checkFile(TMP_DATA_FILE_PATH + TMP_DATA_FILE_NAME, 0)) {
			if (!ReadText(TMP_DATA_FILE_PATH + TMP_DATA_FILE_NAME)) {
				envErrorFlag = true;
				mCurrentID = getResources().getString(
						R.string.main_folder_create_error);
			}
		}

		if (nfcAdapter == null) {
			mCurrentID = getResources().getString(R.string.main_nonfc_warning);
		} else if (!nfcAdapter.isEnabled()) {
			mCurrentID = getResources().getString(
					R.string.main_nfcclose_warning);
		}

		if (!hasSdcard()) {
			envErrorFlag = true;
			mCurrentID = getResources().getString(R.string.main_nonfc_warning);
		}

		if (!checkDataPath()) {
			envErrorFlag = true;
			mCurrentID = getResources().getString(
					R.string.main_folder_create_error);
		}

		mStatus = "";
		mCurrentID = "";

		bottomBar.setSelectedState(mCurrentFG);
	}

	@Override
	public void onBackPressed() {
		dialog();
	}

	protected void dialog() {
		Dialog dialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.btn_star)
				.setTitle(getResources().getString(R.string.menu_title))
				.setPositiveButton(getResources().getString(R.string.menu_item_1), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						removeTmpDataFile();
						System.exit(0);
					}
				}).setNeutralButton(getResources().getString(R.string.menu_item_2), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						System.exit(0);
					}
				}).setNegativeButton(getResources().getString(R.string.menu_item_3), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	@Override
	public void onShowSaveDialog() {
		// TODO Auto-generated method stub
		dialogSave();
	}

	protected void dialogSave() {
		final EditText etDataFileName = new EditText(this);
		Dialog dialog = new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.history_save_dialog_title))
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(etDataFileName)
				.setPositiveButton(getResources().getString(R.string.history_save_dialog_save), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String dataFileName = etDataFileName.getText().toString().trim();
						if (dataFileName.length() == 0) {
							dataFileName = "default";
							etDataFileName.setText(dataFileName);
						}
						String DataFile = DATA_FILE_PATH + dataFileName + ".txt";
						for (int i = 0; i < mIDList.size(); i++) {
							if (!checkFile(DataFile, 1)
									|| !appendSDFile(DataFile, mIDList.get(i) + "\r\n")) {
								mCurrentID = getResources().getString(
										R.string.main_folder_create_error);
							}
						}
						removeTmpDataFile();
						mIDList.clear();
						mStatus = "";
						mCurrentID = "";
						showDetails();
						dialog.dismiss();
					}
				})
				.setNegativeButton(getResources().getString(R.string.history_save_dialog_cancel), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void removeTmpDataFile() {
		if (checkFile(TMP_DATA_FILE_PATH + TMP_DATA_FILE_NAME, 0)) {
			File file = new File(TMP_DATA_FILE_PATH + TMP_DATA_FILE_NAME);
			file.delete();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (nfcAdapter == null) {
			// msg.setText(R.string.main_nonfc_warning);
			return;
		}
		nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
				mTechLists);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// 处理该intent
		if (!envErrorFlag && nfcAdapter.isEnabled() && mCurrentFG == 0) {
			processIntent(intent);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (nfcAdapter == null) {
			// msg.setText(R.string.main_nonfc_warning);
			return;
		}
		nfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	@SuppressLint("NewApi")
	private void processIntent(Intent intent) {
		// 取出封装在intent中的TAG
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		for (String tech : tagFromIntent.getTechList()) {
			System.out.println(tech);
		}
		// 读取TAG
		MifareUltralight mfc = MifareUltralight.get(tagFromIntent);
		try {
			// Enable I/O operations to the tag from this TagTechnology object.
			mfc.connect();

			int type = mfc.getType();// 获取TAG的类型

			if (type != MifareUltralight.TYPE_ULTRALIGHT) {
				return;
			}

			mCurrentID = getNfcID(mfc.readPages(0), mfc.readPages(1)).toUpperCase();
			if (mStatus.equals(getResources().getString(
					R.string.scan_text_title_button_1))
					|| mStatus.equals(getResources().getString(
							R.string.scan_text_title_button_3))) {
				if (mIDList.indexOf(mCurrentID) != -1) {
					mCurrentID = mCurrentID
							+ getResources().getString(
									R.string.scan_text_current_exist);
				} else {
					String tmpDataFile = TMP_DATA_FILE_PATH
							+ TMP_DATA_FILE_NAME;
					if (!checkFile(tmpDataFile, 1)
							|| !appendSDFile(tmpDataFile, mCurrentID + "\r\n")) {
						mCurrentID = getResources().getString(
								R.string.main_folder_create_error);
					} else {
						mIDList.add(mCurrentID);
					}
				}
			}
			showDetails();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getNfcID(byte[] pageload1, byte[] pageload2) {
		StringBuilder stringBuilder = new StringBuilder();
		if (pageload1 == null || pageload1.length <= 0 || pageload2 == null
				|| pageload2.length <= 0) {
			return null;
		}

		char[] buffer = new char[2];
		for (int i = 0; i < 3; i++) {
			buffer[0] = Character.forDigit((pageload1[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(pageload1[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		for (int i = 0; i < 4; i++) {
			buffer[0] = Character.forDigit((pageload2[i] >>> 4) & 0x0F, 16);
			buffer[1] = Character.forDigit(pageload2[i] & 0x0F, 16);
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}

		return stringBuilder.toString();
	}

	/**
	 * @FunName showDetails
	 * @Description switch the fragment content according to the selected item
	 *              on bottombar
	 * @param index
	 * @return N/A
	 * 
	 */
	private void showDetails() {
		Fragment details = (Fragment) getSupportFragmentManager()
				.findFragmentById(R.id.main_details);

		// set the target fragment according to the index
		switch (mCurrentFG) {
		case BTN_ITEM_SCAN:
			details = new FragmentScan();
			break;
		case BTN_ITEM_HISTORY:
			details = new FragmentHistory();
			break;
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.main_details, details);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		Bundle args = new Bundle();
		if (mCurrentFG == BTN_ITEM_SCAN) {
			if (mStatus != null) {
				args.putString(FragmentScan.SCAN_STATUS, mStatus);
			}
			if (mCurrentID != null) {
				args.putString(FragmentScan.SCAN_ID, mCurrentID);
			}
		}
		if (mIDList != null) {
			args.putStringArrayList(FragmentScan.SCAN_ID_LIST, mIDList);
		}
		details.setArguments(args);
		ft.commit();
	}

	@Override
	public void onUpdateStatus(String pStatus) {
		mStatus = pStatus;
	}

	@Override
	public void onUpdateList(ArrayList<String> newList) {
		// TODO Auto-generated method stub
		mIDList = newList;

		removeTmpDataFile();
		String tmpDataFile = TMP_DATA_FILE_PATH
				+ TMP_DATA_FILE_NAME;
		for (int i = 0; i < mIDList.size(); i++) {
			if (!checkFile(tmpDataFile, 1)
					|| !appendSDFile(tmpDataFile, mIDList.get(i) + "\r\n")) {
				mCurrentID = getResources().getString(
						R.string.main_folder_create_error);
			}
		}
	}

	/**
	 * @FunName hasSdcard
	 * @Description check if SD card exist or not
	 * @param N
	 *            /A
	 * @return true/false
	 * 
	 */
	private static boolean hasSdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isFolderExists(String strFolder) {
		File file = new File(strFolder);

		if (!file.exists()) {
			if (file.mkdir()) {
				return true;
			} else
				return false;
		}
		return true;
	}

	private boolean checkDataPath() {
		// check app folder
		if (!isFolderExists(APP_PATH)) {
			return false;
		}

		// check app tmp data folder
		if (!isFolderExists(TMP_DATA_FILE_PATH)) {
			return false;
		}

		// check app data folder
		if (!isFolderExists(DATA_FILE_PATH)) {
			return false;
		}

		return true;
	}

	private boolean checkFile(String fileName, int type) {
		File file = new File(fileName);
		if (file == null || !file.exists()) {
			if (type == 0) {
				return false;
			} else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean appendSDFile(String fileName, String content) {
		File file = new File(fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file, true);
			byte[] bytes = content.getBytes();
			fos.write(bytes);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}

	public boolean ReadText(String fileName) {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			return false;
		}

		if (res.length() > 0) {
			String tmpIDList[] = res.split("\n");
			for (String id : tmpIDList) {
				if (id.trim().length() > 0) {
					mIDList.add(id.trim());
				}
			}
		}

		return true;
	}
}
