package com.jasonz.cordova.bugly;

import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import android.util.Log;
import android.webkit.WebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Application;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.bugly.crashreport.CrashReport.UserStrategy;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

public class CDVBugly extends CordovaPlugin {
    public static final String TAG = "Cordova.Plugin.Bugly";
    private String APP_ID;
    private static final String BUGLY_APP_ID = "ANDROID_APPID";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        APP_ID = webView.getPreferences().getString(BUGLY_APP_ID,"");

        Beta.autoCheckUpgrade = false;
		Beta.upgradeStateListener = new UpgradeStateListener() {
			@Override
			public void onUpgradeSuccess(boolean isManual) {
				if (callbackContext != null) {
					UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
					if (upgradeInfo != null) {
						try {
							JSONObject result = new JSONObject();
							result.put("title", upgradeInfo.title);
							// 升级说明
							result.put("newFeature", upgradeInfo.newFeature);
							result.put("versionCode", upgradeInfo.versionCode);
							result.put("versionName", upgradeInfo.versionName);
							result.put("apkUrl", upgradeInfo.apkUrl);
							result.put("fileSize", upgradeInfo.fileSize);

							//			info.append("id: ").append(upgradeInfo.id).append("\n");
							//			info.append("标题: ").append(upgradeInfo.title).append("\n");
							//			info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
							//			info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
							//			info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
							//			info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
							//			info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
							//			info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
							//			info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
							//			info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
							//			info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
							//			info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
							//			info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
							//			info.append("图片地址：").append(upgradeInfo.imageUrl);
							callbackContext.success(result);
						} catch (Exception ex) {
							callbackContext.error(ex.getMessage());
						}
					}
				}
			}

			@Override
			public void onUpgradeFailed(boolean isManual) {
				Log.d(TAG, "UPGRADE_FAILED");
			}

			@Override
			public void onUpgrading(boolean isManual) {
				Log.d(TAG, "UPGRADE_CHECKING");
			}

			@Override
			public void onDownloadCompleted(boolean b) {
				Log.d(TAG, "DownloadCompleted");
			}

			@Override
			public void onUpgradeNoVersion(boolean isManual) {
				if (callbackContext != null)
					callbackContext.success("");
			}
		};
		Bugly.init(this.cordova.getActivity().getApplicationContext(), APP_ID, false);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if(action.equals("initSDK")) {
            return this.initSDK(args, callbackContext);
        } else if (action.equals("enableJSMonitor")){
            return this.enableJSMonitor(args, callbackContext);
        } else if (action.equals("setTagID")){
            return this.setTagID(args, callbackContext);
        } else if (action.equals("setUserID")){
            return this.setUserID(args, callbackContext);
        } else if (action.equals("putUserData")){
            return this.putUserData(args, callbackContext);
        } else if (action.equals("testJavaCrash")){
            return this.testJavaCrash(args, callbackContext);
        } else if (action.equals("testNativeCrash")){
            return this.testNativeCrash(args, callbackContext);
        } else if (action.equals("testANRCrash")){
            return this.testANRCrash(args, callbackContext);
        }else if (action.equals("checkUpgrade")) {
			return this.checkUpgrade(callbackContext);
		}else if (action.equals("getAppInfo")) {
			return this.getAppInfo(callbackContext);
		}
        return false;
    }

    private boolean initSDK(CordovaArgs args, CallbackContext callbackContext) {
        final JSONObject params;

        try {
            params = args.getJSONObject(0);
            //TODO check param format
            UserStrategy strategy = new UserStrategy(this.cordova.getActivity().getApplicationContext());

            if(params.has("channel")) {
                strategy.setAppChannel(params.getString("channel"));
            }
            if(params.has("version")) {
                strategy.setAppVersion(params.getString("version"));
            }
            if(params.has("package")) {
                strategy.setAppPackageName(params.getString("package"));
            }
            if(params.has("delay")) {
                strategy.setAppReportDelay(params.getInt("delay"));
            }
            if(params.has("develop")) {
                CrashReport.setIsDevelopmentDevice(this.cordova.getActivity().getApplicationContext(),params.getBoolean("develop"));
            } else {
                CrashReport.setIsDevelopmentDevice(this.cordova.getActivity().getApplicationContext(), BuildConfig.DEBUG);
            }

            boolean debugModel;

            if(params.has("debug")) {
                debugModel = params.getBoolean("debug");
            } else {
                debugModel = BuildConfig.DEBUG;
            }

            CrashReport.initCrashReport(this.cordova.getActivity().getApplicationContext(),APP_ID,debugModel,strategy);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        Log.d(TAG, "bugly sdk init success!");
        callbackContext.success();
        return true;
    }

    private boolean enableJSMonitor(CordovaArgs args, CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CrashReport.setJavascriptMonitor((WebView)webView.getView(), true);
            }
        });
        callbackContext.success();
        return true;
    }

    private boolean setTagID(CordovaArgs args, CallbackContext callbackContext) {
        try {
            int id = args.getInt(0);
            CrashReport.setUserSceneTag(this.cordova.getActivity().getApplicationContext(), id);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        callbackContext.success();
        return true;
    }

     private boolean setUserID(CordovaArgs args, CallbackContext callbackContext) {
        try {
            String id = args.getString(0);
            CrashReport.setUserId(this.cordova.getActivity().getApplicationContext(), id);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        callbackContext.success();
        return true;
    }

    private boolean putUserData(CordovaArgs args, CallbackContext callbackContext) {
        try {
            String key = args.getString(0);
            String value = args.getString(1);
            CrashReport.putUserData(this.cordova.getActivity().getApplicationContext(), key, value);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }
        callbackContext.success();
        return true;
    }

    private boolean testJavaCrash(CordovaArgs args, CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CrashReport.testJavaCrash();
            }
        });
        callbackContext.success();
        return true;
    }

    private boolean testNativeCrash(CordovaArgs args, CallbackContext callbackContext) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CrashReport.testNativeCrash();
            }
        });
        callbackContext.success();
        return true;
    }

    private boolean testANRCrash(CordovaArgs args, CallbackContext callbackContext) {

        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CrashReport.testANRCrash();
            }
        });
        callbackContext.success();
        return true;
    }

	private boolean checkUpgrade(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
		Beta.checkUpgrade();
        return true;
	}

	private boolean getAppInfo(CallbackContext callbackContext) {
		try {
			PackageManager packageManager = this.cordova.getActivity().getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					this.cordova.getActivity().getPackageName(), 0);
			JSONObject result = new JSONObject();
			result.put("versionName", packageInfo.versionName);
			result.put("versionCode", String.valueOf(packageInfo.versionCode));
			callbackContext.success(result);
		} catch (Exception e) {
			callbackContext.error(e.getMessage());
		}
        return true;
	}
}