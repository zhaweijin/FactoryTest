package com.hiveview.factorytest;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

 





import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hiveview.manager.UsbDeviceManager;
import com.hiveview.manager.Usbdevice;

import android.amlogic.Tv;
import android.content.Context;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetDevInfo;
import android.net.DhcpInfo;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Utils {

	public static String TAG = "Utils";
	private final static String FB0FILE1 = "/sys/class/aml_keys/aml_keys/key_name";
	private final static String FB0FILE2 = "/sys/class/aml_keys/aml_keys/key_write";
	private static FileOutputStream mOutputStream1 = null;
	private static FileOutputStream mOutputStream2 = null;
	
	public static void print(String TAG,String value){
		Log.v(TAG, value);
	}
	
	public static String takeScreenShot() {

		String mSavedPath = Environment.getExternalStorageDirectory()+ File.separator + "screenshot.png";
		try {
			Runtime.getRuntime().exec("screencap -p " + mSavedPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mSavedPath;
	}
    
    
	public static boolean pingHost(String str) {
		boolean isConnected = false;
		 
		try {
			Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + str);
			int status = p.waitFor();
			if (status == 0) {
				isConnected = true;
			} else {
				isConnected = false;
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}

		return isConnected;
	}
	
//	public void testEthernet(Context mContext){
//		
//		EthernetDevInfo info = new EthernetDevInfo();
//		EthernetManager mEthernetManager = (EthernetManager)mContext.getSystemService("ethernet");
//		
//		DhcpInfo mEthernetDhcpInfo = mEthernetManager.getDhcpInfo();
//		String mDNSAddress = getAddress(mEthernetDhcpInfo.dns1);
//		
//		//如果之前是手动模式，先切换到动态DHCP模式
//		if (info.getConnectMode().equals(EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
//			info.setIfName("eth0");
//			info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
//			info.setIpAddress(null);
//			info.setRouteAddr(null);
//			info.setDnsAddr(mDNSAddress);
//			info.setNetMask(null);
//			Utils.print(TAG, "set ethernet dhcp");
//		}
//		mEthernetManager.updateEthDevInfo(info);
// 
//	}
	
	
	public static String getAddress(int addr) {
		return intToInetAddress(addr).getHostAddress();
	}
	
	public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                                (byte)(0xff & (hostAddress >> 8)),
                                (byte)(0xff & (hostAddress >> 16)),
                                (byte)(0xff & (hostAddress >> 24)) };

        try {
           return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
           throw new AssertionError();
        }
    }
	
	
	//从电源口开始，USB4 USB2 USB1
	/*
	 * private String USB1 = "1";
	 * private String USB2 = "2";
	 * private String USB4 = "4";
	 * port=1 or 2 or 4 
	 */
	public boolean testUsb(String port) {
		boolean result = false;
		try {
			UsbDeviceManager usbDeviceManager = UsbDeviceManager.getUsbDeviceManager();
			Log.v(TAG, "usb devices size="+ usbDeviceManager.getUsbDevicePool().size());

			Usbdevice testuUsbdevice = null;

			for (int i = 0; i < usbDeviceManager.getUsbDevicePool().size(); i++) {
				Usbdevice usbdevice = usbDeviceManager.getUsbDevicePool().get(i);
				Log.v(TAG, "usburi==" + usbdevice.usburi);
				Log.v(TAG, "usbport==" + usbdevice.usbport);

				if (usbdevice.usbport != null && usbdevice.usbport.equals(port)) {
					testuUsbdevice = usbdevice;
					break;
				}
			}

			if (testuUsbdevice == null)
				return result;

			if (writeData(testuUsbdevice.usburi, "writetest.txt", "writetest")
					&& readData(testuUsbdevice.usburi, "test.txt", "test")) {
				result = true;
				Utils.print(TAG, port + "===read and write ok");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	  }
		
	/*
	 * @function 写数据到U盘
	 * filePath  文件的路径
	 * fileName  文件名
	 * content 文件内容，用以比对读取的是否正确
	 */
		private static boolean writeData(final String filePath, final String fileName, final String content){
			boolean result = false;
			int FILE_CONTENT_LEN = 256;
			File file = new File(filePath+File.separator+fileName);
			Utils.print(TAG, "writefile=="+file.getAbsolutePath());
			if(null != file){
					    
				FileOutputStream outStream = null;
		    	try {
		    		if(!file.exists()){
						file.createNewFile();
					}

		    		outStream = new FileOutputStream(file);
		    		byte[] buffer= new byte[FILE_CONTENT_LEN];
		    		buffer = content.getBytes();
		    		final int len = buffer.length;
		    		Log.d(TAG, "write buffer.len." + len);
		    		if(len <= FILE_CONTENT_LEN){
		    			outStream.write(buffer, 0, len);
		    		}
					outStream.close();
					outStream = null;
					result = true;
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(null != outStream){
						try {
							outStream.close();
							outStream = null;
						} catch (Exception e2) {
							e2.printStackTrace();
						}						
					}
				}
			}
			Utils.print(TAG, "write result=="+result);
			return result;
		}
		
		
		/*
		 * @function 从U盘读数据
		 * filePath 文件路径
		 * fileName 文件名
		 * content 读取的比对内容
		 */
		private boolean readData(final String filePath, final String fileName, final String content){
			boolean result = false;
			int FILE_CONTENT_LEN = 256;
			File file = new File(filePath+File.separator+fileName);
			if(null != file){
				if(file.exists()){
					FileInputStream inStream = null;	    	
			    	try {
			    		byte[] buffer= new byte[FILE_CONTENT_LEN];		    	
			    		inStream = new FileInputStream(file);
			    		int len = 0, totalLen = 0;
			    		while(((len = inStream.read(buffer))!= -1)){
			    			totalLen += len;
//			        		if(DEBUG) Log.d(TAG, "read len." + len + " totalLen." + totalLen);
			        	}		    		
			    		inStream.close();
			    		inStream = null;
			    		if(totalLen <= FILE_CONTENT_LEN){
							String readContent = new String(buffer, 0, totalLen);
							Log.d(TAG, "content:" + content + " readContent:" + readContent);					
							if(content.trim().equals(readContent.trim())){
								result = true;
							}
			    		}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if(null != inStream){
							try {
								inStream.close();
								inStream = null;
							} catch (Exception e2) {
								e2.printStackTrace();
							}						
						}
					}
				}			
			}
			Utils.print(TAG, "read result=="+result);
			return result;
		}
		
		
		/*
		 * @function写云盒的MAC or SN
		 * content 为写入的内容
		 * flag 标识为 mac 或者是 sn 需要小写
		 * return 成功为0 失败为-1
		 */
		public static int writeMACorSN(String content, String flag) {
			File mFile1 = new File(FB0FILE1);
			File mFile2 = new File(FB0FILE2);
			if (mFile1.exists() && mFile2.exists()) {
				String mContent = null;
				if (null != content) {
					char[] mChar = content.toCharArray();

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
						if(writeFile(mOutputStream1, flag)==-1)
							return -1;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return -1;
					}

					// 写入key_write
					try {
						mOutputStream2 = new FileOutputStream(mFile2);
						if(writeFile(mOutputStream2, mContent)==-1)
							return -1;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return -1;
					}
 
					return 0;
				} else {
					return -1;
				}
			}
			return -1;
		}
		
		
		
		/*
		 * @function写云盒的HDCP
		 * content 为写入的HDCP File
		 * return 成功为0 失败为-1
		 */
		private int writeHdcp(File hdcpFile) {
			
			byte[] binContent = getBinContent(hdcpFile);
			if (binContent != null) {
				return -1;
			}
		    String content = bytes2HexString(binContent);
			
			
			String HDCP = "rxhdcp20";
			File mFile1 = new File(FB0FILE1);
			File mFile2 = new File(FB0FILE2);
			if (mFile1.exists() && mFile2.exists()) {
				if (null != content) {
					// 写入key_name
					try {
						mOutputStream1 = new FileOutputStream(mFile1);
						if(writeFile(mOutputStream1, HDCP)==-1)
							return -1;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return -1;
					}

					// 写入key_write
					try {
						mOutputStream2 = new FileOutputStream(mFile2);
						if(writeFile(mOutputStream2, content)==-1)
							return -1;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return -1;
					}
					return 0;
				} else {
					return -1;
				}
			}
			return -1;
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
			
			
			/*
			 * @function写云盒的MODEL
			 * content 为写入的内容
			 * return 成功为0 失败为-1
			 */
			private int writeModel(String content) {
				String MODEL = "model";
				
				File mFile1 = new File(FB0FILE1);
				File mFile2 = new File(FB0FILE2);
				if (mFile1.exists() && mFile2.exists()) {
					String mContent = null;
					int length = content.length();
					length = length * 2;
					String mLength = String.valueOf(length);
					if (null != content) {
						char[] mChar = content.toCharArray();

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
							if(writeFile(mOutputStream1, MODEL)==-1)
								return -1;
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return -1;
						}
						
						// 写入key_write
						try {
							mOutputStream2 = new FileOutputStream(mFile2);
							if(writeFile(mOutputStream2, mContent)==-1)
								return -1;
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							return -1;
						}
						
						return 0;
					} else {
						return -1;
					}
				}
				return -1;
			}
			
			/*
			 * @function 写入云屏SN
			 * content 为写入内容
			 */
			private void writeYunPingSN(String content) {
				Tv tv = Tv.open();
				tv.FactorySet_FBC_SN_Info(content, content.length());
			}
			
			private static int writeFile(OutputStream outputStream,String content){
				int result = -1;
			 
				DataOutputStream mDoutPutStream2 = new DataOutputStream(outputStream);
				try {
					mDoutPutStream2.writeBytes(content);
					mDoutPutStream2.close();
					result = 0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			}
			
			
			
			
}
