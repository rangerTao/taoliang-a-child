package com.duole.utils;

import java.util.List;

import com.duole.R;
import com.duole.launcher.*;
import com.duole.service.download.dao.WidgetDao;

import android.R.string;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class WidgetUtils {
	
	static AppWidgetManager mAppWidgetManager;

	public static View getWidgetViewByWidgetID(Context mContext, String wid,String packagename){
		
		mAppWidgetManager = AppWidgetManager.getInstance(mContext);
		
		LauncherAppWidgetHost mAppWidgetHost = new LauncherAppWidgetHost(mContext, 1034);
		mAppWidgetHost.startListening();
		
		
		int appwidgetid = Integer.parseInt(wid);
		
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appwidgetid);
		
		if(appWidgetInfo == null){
			Log.e("TAG", "null");
			getWidgetViewByWidgetPackageName(mContext, packagename);
		}

		// Calculate the grid spans needed to fit this widget

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appwidgetid);

		// Perform actual inflation because we're live
		launcherInfo.hostView = mAppWidgetHost.createView(mContext,
				appwidgetid, appWidgetInfo);

		launcherInfo.hostView.setAppWidget(appwidgetid, appWidgetInfo);
		launcherInfo.hostView.setTag(launcherInfo);
		
		return launcherInfo.hostView;
	}
	
	public static View getWidgetViewByWidgetPackageName(Context mcontext,String packagename){
		LauncherAppWidgetHost mAppWidgetHost = new LauncherAppWidgetHost(mcontext, 1034);
		mAppWidgetHost.startListening();
		
		mAppWidgetManager = AppWidgetManager.getInstance(mcontext);
		List<AppWidgetProviderInfo> widgetInfos = mAppWidgetManager.getInstalledProviders();
		
		int appwidgetid = mAppWidgetHost.allocateAppWidgetId();

		for (int i = 0; i < widgetInfos.size(); i++) {
			
			if(!widgetInfos.get(i).provider.getPackageName().equals(packagename)){
				continue;
			}else{
				mAppWidgetManager.bindAppWidgetId(appwidgetid, widgetInfos.get(i).provider);
			}
			
		}
		
		WidgetDao wd = new WidgetDao(mcontext);
		wd.save(packagename, appwidgetid + "");
		
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appwidgetid);
		
		if(appWidgetInfo == null){
			Log.e("TAG", "null");
		}

		// Calculate the grid spans needed to fit this widget

		// Build Launcher-specific widget info and save to database
		LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(
				appwidgetid);

		// Perform actual inflation because we're live
		launcherInfo.hostView = mAppWidgetHost.createView(mcontext,
				appwidgetid, appWidgetInfo);

		launcherInfo.hostView.setAppWidget(appwidgetid, appWidgetInfo);
		launcherInfo.hostView.setTag(launcherInfo);
		
		return launcherInfo.hostView;
	}

	public static void startConfigureActivityByPkgName(Context context,
			String pkg) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		try {
			mAppWidgetManager = AppWidgetManager.getInstance(context);
			List<AppWidgetProviderInfo> widgetInfos = mAppWidgetManager.getInstalledProviders();
			

			for (int i = 0; i < widgetInfos.size(); i++) {
				
				if(!widgetInfos.get(i).provider.getPackageName().equals(pkg)){
					continue;
				}else{
					if(widgetInfos.get(i).configure != null){
						intent.setComponent(widgetInfos.get(i).configure);
						context.startActivity(intent);
					}
					
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
