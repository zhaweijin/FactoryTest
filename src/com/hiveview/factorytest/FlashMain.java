package com.hiveview.factorytest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.amlogic.Tv;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

 
import com.hiveview.manager.SystemInfoManager;

public class FlashMain extends Activity {
	private String TAG = "WSN";
	private Button mButton0;
	private Button mButton1;
	private Button mButton2;
	private Button mButton3;

	private EditText mEdit1;
	private EditText mEdit2;
	private Context mContext = this;
	private final static String FB0FILE1 = "/sys/class/aml_keys/aml_keys/key_name";
	private final static String FB0FILE2 = "/sys/class/aml_keys/aml_keys/key_write";
	private final static String FB0FILE3 = "/sys/class/aml_keys/aml_keys/key_read";
	private final String MACFILE = "/storage/external_storage/sda1/mac.txt";
	private final String MACID = "/storage/external_storage/sda1/macid.txt";
	private final String SNFILE = "/storage/external_storage/sda1/sn.txt";
	private final String YUNSNFILE = "/storage/external_storage/sda1/yunsn.txt";
	private final String YUNSNID = "/storage/external_storage/sda1/yunsnid.txt";
	private final String SNID = "/storage/external_storage/sda1/snid.txt";
	private final String MODELFILE = "/storage/external_storage/sda1/model.txt";
	private final String HDCPNAME = "/storage/external_storage/sda1/hdcp_name.txt";
	private final String HDCPID = "/storage/external_storage/sda1/hdcp_id.txt";
	private final String DEFAULT_ADDRESS = "/storage/external_storage/sda1/";
	private File mFile1;
	private File mFile2;
	private TextView mText1;
	private TextView mText2;
	private TextView mText3;
	private TextView mText4_for_Hdcp;
	private TextView mText5;
	private TextView mSn_status;
	private TextView mMac_status;
	private TextView mHdcp_status;
	private TextView mModel_status;
	private TextView mYunSn_status;
	File mSnIdFile;
	File mMacIdFile;
	File mHdcpIdFile;

	private SystemInfoManager manager;
	private FileOutputStream mOutputStream1 = null;
	private FileOutputStream mOutputStream2 = null;
	private final String SN = "sn";
	private final String MAC = "mac";
	private final String HDCP = "rxhdcp20";
	private final String MODEL = "model";
	private final String YUNSN = "yunsn";
	int mid = 0;
	int macid = 0;
	int hdcpid = 0;
	int yunsnid = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mButton0 = (Button) findViewById(R.id.button_for_all);
		mButton1 = (Button) findViewById(R.id.button);
		mButton2 = (Button) findViewById(R.id.button2);
		mButton3 = (Button) findViewById(R.id.uninstall);
		mButton1.setOnClickListener(button);
		mButton2.setOnClickListener(button);
		mButton0.setOnClickListener(button);
		mButton3.setOnClickListener(button);

		mEdit1 = (EditText) findViewById(R.id.edittext1);
		mEdit2 = (EditText) findViewById(R.id.edittext2);
		mText1 = (TextView) findViewById(R.id.sn_show);
		mText2 = (TextView) findViewById(R.id.mac_show);
		mText3 = (TextView) findViewById(R.id.model_show);
		mText4_for_Hdcp = (TextView) findViewById(R.id.hdcp_show);
		mText5 = (TextView) findViewById(R.id.yunsn_show);

		mSn_status = (TextView) findViewById(R.id.sn_status);
		mMac_status = (TextView) findViewById(R.id.mac_status);
		mHdcp_status = (TextView) findViewById(R.id.hdcp_status);
		mModel_status = (TextView) findViewById(R.id.model_status);
		mYunSn_status = (TextView) findViewById(R.id.yunsn_status);
		mFile1 = new File(FB0FILE1);
		mFile2 = new File(FB0FILE2);

		// String a = getYUNSNFromTv();
		// mText5.setText(mContext.getResources().getString(R.string.yunsn) +
		// a);

		/*
		 * for (int i = 0; i < 1000; i++) { Log.i(TAG, "------" + getYUNSN() +
		 * "--------"); yunsnid++; }
		 */

		
		 if (getHdcpFlag() == 0x31) { finish(); }

	}

	private int getHdcpFlag() {
		File mName = new File(FB0FILE1);
		File mRead = new File(FB0FILE3);
		FileInputStream mInputStream = null;
		FileOutputStream mOutputStream = null;
		byte[] bflag = new byte[1];
		if (mName.exists() && mRead.exists()) {
			try {
				mOutputStream = new FileOutputStream(mName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			DataOutputStream dOutStream = new DataOutputStream(mOutputStream);
			try {
				dOutStream.writeBytes("manufactory");
				dOutStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			try {
				mInputStream = new FileInputStream(mRead);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
			DataInputStream dInStream = new DataInputStream(mInputStream);
			try {
				dInStream.readFully(bflag);
				dInStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return 0;
			}

			return (int) bflag[0] & 0xFF;
		}
		return 0;
	}

	private static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	private void setColor(String type, boolean k) {
		if (k) {
			if (type == SN) {
				mSn_status.setTextColor(mContext.getResources().getColor(R.color.green));
				mSn_status.setText(mContext.getResources().getString(R.string.sn_ok));
			} else if (type == MAC) {
				mMac_status.setTextColor(mContext.getResources().getColor(R.color.green));
				mMac_status.setText(mContext.getResources().getString(R.string.mac_ok));
			} else if (type == HDCP) {
				mHdcp_status.setTextColor(mContext.getResources().getColor(R.color.green));
				mHdcp_status.setText(mContext.getResources().getString(R.string.hdcp_ok));
			} else if (type == MODEL) {
				mModel_status.setTextColor(mContext.getResources().getColor(R.color.green));
				mModel_status.setText(mContext.getResources().getString(R.string.model_ok));
			} else if (type == YUNSN) {

				mYunSn_status.setTextColor(mContext.getResources().getColor(R.color.green));
				mYunSn_status.setText(mContext.getResources().getString(R.string.yunsn_ok));
			}
		} else {
			if (type == SN) {
				mSn_status.setTextColor(mContext.getResources().getColor(R.color.red));
				mSn_status.setText(mContext.getResources().getString(R.string.sn_error));
			} else if (type == MAC) {
				mMac_status.setTextColor(mContext.getResources().getColor(R.color.red));
				mMac_status.setText(mContext.getResources().getString(R.string.mac_error));
			} else if (type == HDCP) {
				mHdcp_status.setTextColor(mContext.getResources().getColor(R.color.red));
				mHdcp_status.setText(mContext.getResources().getString(R.string.hdcp_error));
			} else if (type == MODEL) {
				mModel_status.setTextColor(mContext.getResources().getColor(R.color.red));
				mModel_status.setText(mContext.getResources().getString(R.string.model_error));
			} else if (type == YUNSN) {
				mYunSn_status.setTextColor(mContext.getResources().getColor(R.color.red));
				mYunSn_status.setText(mContext.getResources().getString(R.string.yunsn_error));
			}

		}
	}

	private View.OnClickListener button = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			String text1 = null;
			String mWrite = null;
			switch (id) {
			case (R.id.button): {
				text1 = mEdit1.getText().toString().replace(" ", "");
				if ("".equals(text1) || null == text1) {
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					mWrite = SN;
					int a = writeMACorSN(text1, mWrite);
					if (a == 0) {
						Toast toast = new Toast(mContext);
						toast = Toast.makeText(mContext,
								mContext.getString(R.string.write_success), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
				break;
			}
			case (R.id.uninstall): {
				// Uri packageUri = Uri.parse("package:" +
				// MainActivity.this.getPackageName());

				// Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
				// startActivity(intent);
				mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
				break;
			}
			case (R.id.button2): {
				text1 = mEdit2.getText().toString().replace(" ", "");
				if ("".equals(text1) || null == text1) {
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					mWrite = MAC;
					int a = writeMACorSN(text1, mWrite);
					if (a == 0) {
						Toast toast = new Toast(mContext);
						toast = Toast.makeText(mContext,
								mContext.getString(R.string.write_success), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
				break;
			}
			case (R.id.button_for_all): {
				manager = SystemInfoManager.getSystemInfoManager();
				String snTemp = getSN();
				String macTemp = getMAC();
				String mYunTemp = getYUNSN();
				int isMacSuccess = -1;
				int isSuccess = -1;
				int isHdcpSuccess = -1;
				int isModelSuccess = -1;
				int isYunSnSuccess = -1;
				// 写入云盒sn
				if (null != snTemp) {
					isSuccess = writeMACorSN(snTemp, SN);
					if (isSuccess == 0) {

						try {
							mText1.setText(mContext.getResources().getString(R.string.sn)
									+ manager.getSnInfo());
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// addId(mSnIdFile, mid);
						setColor(SN, true);
					} else {
						// message1 =
						// mContext.getResources().getString(R.string.sn_error);
						setColor(SN, false);
					}
				} else {
					setColor(SN, false);
					// message1 =
					// mContext.getResources().getString(R.string.sn_error);
				}
				// 写入mac
				if (null != macTemp && isSuccess == 0) {
					isMacSuccess = writeMACorSN(macTemp, MAC);
					if (isMacSuccess == 0) {
						try {
							mText2.setText(mContext.getResources().getString(R.string.mac)
									+ manager.getMacInfo());
						} catch (NotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						setColor(MAC, true);
					} else {
						setColor(MAC, false);

					}
				} else {
					setColor(MAC, false);
				}

				// 写入hdcp
				byte[] mHdcp = getHDCP();
				if (mHdcp != null) {
					String test = bytes2HexString(mHdcp);
					isHdcpSuccess = writeHdcp(test, HDCP);
					// isHdcpSuccess = writeMACorSN(new String(mHdcp), HDCP);
					if (isHdcpSuccess == 0) {

						mText4_for_Hdcp.setText(mContext.getResources().getString(R.string.hdcp1));
						setColor(HDCP, true);
					} else {
						setColor(HDCP, false);
					}
				} else {
					setColor(HDCP, false);
				}

				// 写入model
				String model = getMODEL();
				if (model != null) {
					isModelSuccess = writeModel(model, MODEL);
					if (isModelSuccess == 0) {
						setColor(MODEL, true);
						mText3.setText(mContext.getResources().getString(R.string.model) + model);
					} else {
						setColor(MODEL, false);
					}
				} else {
					setColor(MODEL, false);
				}
				// 写入云屏sn
				if (mYunTemp != null) {
					writeYUNSN(mYunTemp);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String a = getYUNSNFromTv();
					String b = null;
					if (a.length() >=17) {
						b = a.substring(0, 17);
					}
					Log.i(TAG, "----YUNSN----" + a);
					if (null != a && !"".equals(a) && b.equals(mYunTemp)) {
						if (a.length() > 17) {
							a = a.substring(0, 17);
						}
						isYunSnSuccess = 0;

						setColor(YUNSN, true);

						mText5.setText(mContext.getResources().getString(R.string.yunsn) + a);
					} else {
						setColor(YUNSN, false);
					}
				} else {
					setColor(YUNSN, false);
				}

				if (isMacSuccess == 0 && isSuccess == 0 && isModelSuccess == 0
						&& isHdcpSuccess == 0 && isYunSnSuccess == 0) {
					File mFile = new File(YUNSNID);
					addId(mSnIdFile, mid);
					addId(mMacIdFile, macid);
					addId(mHdcpIdFile, hdcpid);
					addId(mFile, yunsnid);
					mButton0.setFocusable(false);
					mButton3.setFocusable(true);
					mButton3.requestFocus();
					writeFactory("1", "manufactory");
				}

				break;
			}
			}

		}
	};

	private void writeYUNSN(String yunsn) {
		Log.i(TAG, "--" + yunsn + "---------------");
		Tv tv = Tv.open();
		tv.FactorySet_FBC_SN_Info(yunsn, yunsn.length());
	}

	private String getYUNSNFromTv() {
		Tv tv = Tv.open();
		return tv.FactoryGet_FBC_SN_Info().STR_SN_INFO;
	}

	public String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	private int writeHdcp(String text1, String mWrite) {
		if (mFile1.exists() && mFile2.exists()) {

			if (null != text1) {

				// 写入key_name
				try {
					mOutputStream1 = new FileOutputStream(mFile1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream = new DataOutputStream(mOutputStream1);
				try {
					mDoutPutStream.writeBytes(mWrite);
					mDoutPutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				// 写入key_write
				try {
					mOutputStream2 = new FileOutputStream(mFile2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream2 = new DataOutputStream(mOutputStream2);
				try {

					mDoutPutStream2.writeBytes(text1);
					mDoutPutStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				/*
				 * Toast toast = new Toast(mContext); toast =
				 * Toast.makeText(mContext,
				 * mContext.getString(R.string.write_success),
				 * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
				 * toast.show();
				 */
				return 0;
			} else {
				Toast toast = new Toast(mContext);
				toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return -1;
			}

		}
		return -1;

	}

	private void addId(File mTempFile, int id) {
		if (!mTempFile.exists()) {
			try {
				mTempFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(mTempFile));
			output.write(String.valueOf(id + 1));
			output.flush();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getMODEL() {
		File mModelFile = new File(MODELFILE);
		if (mModelFile.exists()) {
			FileInputStream mModelStream;
			try {
				mModelStream = new FileInputStream(mModelFile);
				InputStreamReader mModelStreamReader = new InputStreamReader(mModelStream);
				BufferedReader br = new BufferedReader(mModelStreamReader);
				String model = null;
				try {
					model = br.readLine();
					br.close();
					mModelStreamReader.close();
					mModelStream.close();
					return model;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		return null;
	}

	private String getSN() {
		mSnIdFile = new File(SNID);

		// 获取SN id
		if (mSnIdFile.exists()) {
			try {
				FileInputStream mSnIdStream = new FileInputStream(mSnIdFile);
				InputStreamReader mSnIdStreamReader = new InputStreamReader(mSnIdStream);
				BufferedReader br = new BufferedReader(mSnIdStreamReader);
				String mTemp = null;
				try {
					while ((mTemp = br.readLine()) != null) {
						Log.i("MAC", "------" + mTemp);
						mid = Integer.parseInt(mTemp);
					}
					mSnIdStreamReader.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		File mSnFile = new File(SNFILE);
		// String sn = null;
		if (mSnFile.exists()) {
			try {
				FileInputStream mSnStream = new FileInputStream(mSnFile);
				InputStreamReader mSnStreamReader = new InputStreamReader(mSnStream);
				BufferedReader br = new BufferedReader(mSnStreamReader);
				String sn1 = null;
				String sn2 = null;
				try {
					sn1 = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				try {
					sn2 = br.readLine();
					mSnStreamReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

				int snnum = -1;
				String common = sn1.substring(0, 12);
				if (sn1 != null) {
					String a = sn1.substring(12);
					snnum = Integer.parseInt(a);
					snnum = snnum + mid;
				}
				if (sn2 != null) {
					String b = sn2.substring(12);
					int maxSn = Integer.parseInt(b);
					if (snnum > maxSn) {
						return null;
					}
				}
				if (snnum != -1) {
					String c = String.valueOf(snnum);
					int k = c.length();
					for (int i = 0; i < (5 - k); i++) {
						c = "0" + c;
					}

					return common + c;

				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		return null;
	}

	private String getMAC() {
		mMacIdFile = new File(MACID);
		String mmac = null;
		// 获取SN id
		if (mMacIdFile.exists()) {
			try {
				FileInputStream mSnIdStream = new FileInputStream(mMacIdFile);
				InputStreamReader mSnIdStreamReader = new InputStreamReader(mSnIdStream);
				BufferedReader br = new BufferedReader(mSnIdStreamReader);
				String mTemp = null;
				try {
					while ((mTemp = br.readLine()) != null) {
						Log.i("MAC", "------" + mTemp);
						macid = Integer.parseInt(mTemp);
					}
					mSnIdStreamReader.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		File mMacFile = new File(MACFILE);
		if (mMacFile.exists()) {
			try {
				FileInputStream mMacStream = new FileInputStream(mMacFile);
				InputStreamReader mMacStreamReader = new InputStreamReader(mMacStream);
				BufferedReader br = new BufferedReader(mMacStreamReader);
				String mac1 = null;
				String mac2 = null;
				try {
					mac1 = br.readLine();
					mac2 = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				String[] macs = new String[6];
				for (int i = 0; i < 6; i++) {
					macs[i] = mac1.substring(2 * i, 2 * i + 2);
				}
				int[] macsInt = new int[6];
				for (int i = 0; i < 6; i++) {
					macsInt[i] = Integer.parseInt(macs[i], 16);
				}
				// macsInt[5] = macsInt[5] + macid;
				if (macid == 65792) {
					int y = 0;
					y++;
				}
				int k = 0;
				int y = 0;
				int q = 0;
				int w = 0;
				int e = 0;
				int r = 0;
				if (macid != 0) {
					k = macid / 256;
					y = macid % 256;
					macsInt[5] = macsInt[5] + y;
				}
				if (k != 0) {
					q = k / 256;
					w = k % 256;
					macsInt[4] = macsInt[4] + w;
					macsInt[3] = macsInt[3] + q;
				} else {

					macsInt[4] = macsInt[4] + k;
				}

				if (macsInt[5] > 255) {
					macsInt[5] = macsInt[5] - 255 - 1;
					macsInt[4] = macsInt[4] + 1;
				}

				if (macsInt[4] > 255) {
					macsInt[4] = macsInt[4] - 255 - 1;
					macsInt[3] = macsInt[3] + 1;
				}

				if (macsInt[3] > 255) {
					macsInt[3] = macsInt[3] - 255 - 1;
					macsInt[2] = macsInt[2] + 1;
				}
				if (macsInt[2] > 255) {
					macsInt[2] = macsInt[2] - 255 - 1;
					macsInt[1] = macsInt[1] + 1;
				}
				if (macsInt[1] > 255) {
					macsInt[1] = macsInt[1] - 255 - 1;
					macsInt[0] = macsInt[0] + 1;
				}

				if (macsInt[0] > 255) {
					return null;
				}

				String[] newmac = new String[6];

				for (int i = 0; i < 6; i++) {
					newmac[i] = Integer.toHexString(macsInt[i]).toUpperCase();
					if (newmac[i].length() == 1) {
						newmac[i] = "0" + newmac[i];
					}
					if (mmac != null) {
						mmac = mmac + ":" + newmac[i];

					} else {
						mmac = newmac[i];
					}

				}
				if (mmac != null)
					return mmac;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private int writeMACorSN(String text1, String mWrite) {
		if (mFile1.exists() && mFile2.exists()) {
			String mContent = null;
			if (null != text1) {
				char[] mChar = text1.toCharArray();
				/*
				 * if (mChar.length != 17) { Toast toast = new Toast(mContext);
				 * toast = Toast.makeText(mContext,
				 * mContext.getString(R.string.in_type), Toast.LENGTH_SHORT);
				 * toast.setGravity(Gravity.CENTER, 0, 0); toast.show(); return
				 * -1; }
				 */

				for (int i = 0; i < mChar.length; i++) {
					int a = (int) mChar[i];
					String aa = Integer.toHexString(a);
					if (null == mContent) {
						mContent = aa;
					} else {
						mContent = mContent + aa;
					}
				}

				// 写入key_name
				try {
					mOutputStream1 = new FileOutputStream(mFile1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream = new DataOutputStream(mOutputStream1);
				try {
					mDoutPutStream.writeBytes(mWrite);
					mDoutPutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				// 写入key_write
				try {
					mOutputStream2 = new FileOutputStream(mFile2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream2 = new DataOutputStream(mOutputStream2);
				try {
					// Log.i("YQB", mContent);
					mDoutPutStream2.writeBytes(mContent);
					mDoutPutStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				/*
				 * Toast toast = new Toast(mContext); toast =
				 * Toast.makeText(mContext,
				 * mContext.getString(R.string.write_success),
				 * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
				 * toast.show();
				 */
				return 0;
			} else {
				Toast toast = new Toast(mContext);
				toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return -1;
			}

		}
		return -1;
	}

	private int writeFactory(String text1, String mWrite) {
		if (mFile1.exists() && mFile2.exists()) {
			String mContent = text1;
			if (null != text1) {
				char[] mChar = text1.toCharArray();
				/*
				 * if (mChar.length != 17) { Toast toast = new Toast(mContext);
				 * toast = Toast.makeText(mContext,
				 * mContext.getString(R.string.in_type), Toast.LENGTH_SHORT);
				 * toast.setGravity(Gravity.CENTER, 0, 0); toast.show(); return
				 * -1; }
				 */

				/*
				 * for (int i = 0; i < mChar.length; i++) { int a = (int)
				 * mChar[i]; String aa = Integer.toHexString(a); if (null ==
				 * mContent) { mContent = aa; } else { mContent = mContent + aa;
				 * } }
				 */

				// 写入key_name
				try {
					mOutputStream1 = new FileOutputStream(mFile1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream = new DataOutputStream(mOutputStream1);
				try {
					mDoutPutStream.writeBytes(mWrite);
					mDoutPutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				// 写入key_write
				try {
					mOutputStream2 = new FileOutputStream(mFile2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream2 = new DataOutputStream(mOutputStream2);
				try {
					Log.i("YQB", mContent);
					mDoutPutStream2.writeBytes(mContent);
					mDoutPutStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				/*
				 * Toast toast = new Toast(mContext); toast =
				 * Toast.makeText(mContext,
				 * mContext.getString(R.string.write_success),
				 * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
				 * toast.show();
				 */
				return 0;
			} else {
				Toast toast = new Toast(mContext);
				toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return -1;
			}

		}
		return -1;
	}

	private byte[] getHDCP() {
		mHdcpIdFile = new File(HDCPID);
		// 获取HDCP ID
		String content = getFileContent(mHdcpIdFile);
		if (content != null) {
			hdcpid = Integer.parseInt(content);
		} else {

		}
		String filename = getHDCPNAME();
		if (filename != null) {
			String fileaddress = DEFAULT_ADDRESS + getHDCPNAME();
			File hdcpFile = new File(fileaddress);
			byte[] binContent = getBinContent(hdcpFile);
			if (binContent != null) {
				return binContent;
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	private String getHDCPNAME() {
		File hdcpfile = new File(HDCPNAME);
		String content = getFileContent(hdcpfile);
		String name = null;
		if (content == null) {
			content = "";
		}
		name = content + String.valueOf(hdcpid + 1) + ".bin";
		return name;

	}

	private byte[] getBinContent(File mFile) {
		if (mFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(mFile);
				DataInputStream dis = new DataInputStream(in);
				try {
					int size = in.available();
					byte[] b = new byte[size];
					dis.readFully(b);

					Log.i(TAG, DEFAULT_ADDRESS);
					return b;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private String getFileContent(File mFile) {
		if (mFile.exists()) {
			FileInputStream mSnIdStream;
			try {
				mSnIdStream = new FileInputStream(mFile);
				InputStreamReader mSnIdStreamReader = new InputStreamReader(mSnIdStream);
				BufferedReader br = new BufferedReader(mSnIdStreamReader);
				try {
					String content = br.readLine();
					br.close();
					mSnIdStreamReader.close();
					mSnIdStream.close();
					return content;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}
		return null;

	}

	private int writeModel(String text1, String mWrite) {
		if (mFile1.exists() && mFile2.exists()) {
			String mContent = null;
			int length = text1.length();
			length = length * 2;
			String mLength = String.valueOf(length);
			if (null != text1) {
				char[] mChar = text1.toCharArray();

				for (int i = 0; i < mChar.length; i++) {
					int a = (int) mChar[i];
					String aa = Integer.toHexString(a);
					if (null == mContent) {
						mContent = aa;
					} else {
						mContent = mContent + aa;
					}
				}
				mContent = mLength + mContent;

				// 写入key_name
				try {
					mOutputStream1 = new FileOutputStream(mFile1);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream = new DataOutputStream(mOutputStream1);
				try {
					mDoutPutStream.writeBytes(mWrite);
					mDoutPutStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				// 写入key_write
				try {
					mOutputStream2 = new FileOutputStream(mFile2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.read_error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				DataOutputStream mDoutPutStream2 = new DataOutputStream(mOutputStream2);
				try {
					// Log.i("YQB", mContent);
					mDoutPutStream2.writeBytes(mContent);
					mDoutPutStream2.close();
				} catch (IOException e) {
					e.printStackTrace();
					Toast toast = new Toast(mContext);
					toast = Toast.makeText(mContext, mContext.getString(R.string.error),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return -1;
				}

				/*
				 * Toast toast = new Toast(mContext); toast =
				 * Toast.makeText(mContext,
				 * mContext.getString(R.string.write_success),
				 * Toast.LENGTH_SHORT); toast.setGravity(Gravity.CENTER, 0, 0);
				 * toast.show();
				 */
				return 0;
			} else {
				Toast toast = new Toast(mContext);
				toast = Toast.makeText(mContext, mContext.getString(R.string.in_type),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return -1;
			}

		}
		return -1;
	}

	private String getYUNSN() {
		File mSnIdFile = new File(YUNSNID);

		// 获取SN id
		if (mSnIdFile.exists()) {
			try {
				FileInputStream mSnIdStream = new FileInputStream(mSnIdFile);
				InputStreamReader mSnIdStreamReader = new InputStreamReader(mSnIdStream);
				BufferedReader br = new BufferedReader(mSnIdStreamReader);
				String mTemp = null;
				try {
					while ((mTemp = br.readLine()) != null) {
						// Log.i("MAC", "------" + mTemp);
						yunsnid = Integer.parseInt(mTemp);
					}
					mSnIdStreamReader.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		File mSnFile = new File(YUNSNFILE);
		// String sn = null;
		if (mSnFile.exists()) {
			try {
				FileInputStream mSnStream = new FileInputStream(mSnFile);
				InputStreamReader mSnStreamReader = new InputStreamReader(mSnStream);
				BufferedReader br = new BufferedReader(mSnStreamReader);
				String sn1 = null;
				String sn2 = null;
				try {
					sn1 = br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				try {
					sn2 = br.readLine();
					br.close();
					mSnStreamReader.close();
					mSnStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

				int snnum = -1;
				String common = sn1.substring(0, 12);
				if (sn1 != null) {
					String a = sn1.substring(12);
					snnum = Integer.parseInt(a);
					snnum = snnum + yunsnid;
				}
				if (sn2 != null) {
					String b = sn2.substring(12);
					int maxSn = Integer.parseInt(b);
					if (snnum > maxSn) {
						return null;
					}
				}
				if (snnum != -1) {
					String c = String.valueOf(snnum);
					int k = c.length();
					for (int i = 0; i < (5 - k); i++) {
						c = "0" + c;
					}

					return common + c;

				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		return null;
	}
}
