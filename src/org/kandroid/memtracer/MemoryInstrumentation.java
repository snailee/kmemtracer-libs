package org.kandroid.memtracer;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.test.InstrumentationTestRunner;
import android.util.Log;

public class MemoryInstrumentation extends InstrumentationTestRunner {
	private static final String TAG = MemoryInstrumentation.class.getSimpleName();
	
    private static final String OPTION_MAIN_ACTIVITY_CLASS = "activity";
    private static final String OPTION_DEBUG = "debug";

	private MemoryTracer mMemoryTracer;

	private String mMainActivityClass;
	
	private boolean mDebug;
	
	public MemoryInstrumentation() {
		
	}
	
	@Override
	public void onCreate(Bundle arguments) {
		Log.d(TAG, "onCreate()");
		mMainActivityClass = arguments.getString(OPTION_MAIN_ACTIVITY_CLASS);
		mDebug = arguments.getBoolean(OPTION_DEBUG, false);
		mMemoryTracer = createMemoryTracer();
		mMemoryTracer.startTracing(getTargetContext().getPackageName());
		super.onCreate(arguments);
	}
	
	protected MemoryTracer createMemoryTracer() {
		return new MemoryTracer(new MemoryTraceCsvWriter());
	}
	
	@Override
	public void onStart() {
		if (mDebug) Log.d(TAG, "onStart()");
		String mainActivityClass = getMainActivityClass();
		if (mainActivityClass != null && mainActivityClass.length() > 0) {
			launchMainActivity(getTargetContext().getPackageName(), mainActivityClass);
		} else {
			super.onStart();
		}
	}

	protected String getMainActivityClass() {
		return mMainActivityClass;
	}
	
	private final void launchMainActivity(String packageName, String actvityClassName) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(packageName, actvityClassName);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivitySync(intent);
		this.waitForIdleSync();
	}
    
	@Override
	public void onDestroy() {
		if (mDebug) Log.d(TAG, "onDestroy()");
		mMemoryTracer.stopTracing();
		super.onDestroy();
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		String label = activity.getLocalClassName()+"-OnCreate";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnCreate(activity, icicle);
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		String label = activity.getLocalClassName()+"-OnDestroy";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		String label = activity.getLocalClassName()+"-OnNewIntent";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnNewIntent(activity, intent);
	}

	@Override
	public void callActivityOnPause(Activity activity) {
		String label = activity.getLocalClassName()+"-OnPause";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnPause(activity);
	}

	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
		String label = activity.getLocalClassName()+"-OnPostCreate";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnPostCreate(activity, icicle);
	}

	@Override
	public void callActivityOnRestart(Activity activity) {
		String label = activity.getLocalClassName()+"-OnRestart";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnRestart(activity);
	}

	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
		String label = activity.getLocalClassName()+"-OnRestoreInstanceState";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnRestoreInstanceState(activity, savedInstanceState);
	}

	@Override
	public void callActivityOnResume(Activity activity) {
		String label = activity.getLocalClassName()+"-OnResume";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnResume(activity);
	}

	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
		String label = activity.getLocalClassName()+"-OnSaveInstanceState";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnSaveInstanceState(activity, outState);
	}

	@Override
	public void callActivityOnStart(Activity activity) {
		String label = activity.getLocalClassName()+"-OnStart";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnStart(activity);
	}

	@Override
	public void callActivityOnStop(Activity activity) {
		String label = activity.getLocalClassName()+"-OnStop";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnStop(activity);
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {
		String label = activity.getLocalClassName()+"-OnUserLeaving";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callActivityOnUserLeaving(activity);
	}

	@Override
	public void callApplicationOnCreate(Application app) {
		String label = app.getPackageName()+"-OnAppCreate";
		Bundle snapshot = mMemoryTracer.addSnapshot(label);
		if (mDebug) Log.d(TAG, "[" + label + "] " + snapshot.toString());
		super.callApplicationOnCreate(app);
	}
	
}
