package org.kandroid.memtracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class MemoryTraceCsvWriter implements MemoryTracer.ResultsWriter {
	private static final String TAG = MemoryTraceCsvWriter.class.getSimpleName();
	
	private static final String[] DEFAULT_METRIC_KEYS = {
		MemoryTracer.METRIC_KEY_LABEL,
		MemoryTracer.METRIC_KEY_JAVA_ALLOCATED,
		MemoryTracer.METRIC_KEY_JAVA_FREE,
		MemoryTracer.METRIC_KEY_NATIVE_ALLOCATED,
		MemoryTracer.METRIC_KEY_NATIVE_FREE,
	};
	
	private static final String DEFAULT_MEMORY_TRACE_FILE_NAME = "kmemtrace.csv";
	private static final String MEMORY_TRACE_FILE_NAME_PREFIX = "kmemtrace_";
	private static final String MEMORY_TRACE_FILE_NAME_EXT = ".csv";
	private static final SimpleDateFormat MEMORY_TRACE_FILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final String MEMORY_TRACE_FILE_DIR = "kmemtracer";

	private PrintStream mTraceOut;
	private String[] mMetricKeys;
	
	public MemoryTraceCsvWriter() {
		this(DEFAULT_METRIC_KEYS);
	}
	
	public MemoryTraceCsvWriter(String[] metricKeys) {
		mMetricKeys = metricKeys;
	}
	
	@Override
	public void writeTraceStart(String label) {
		Log.d(TAG, "Start tracing for " + label);
		openTraceFile(DEFAULT_MEMORY_TRACE_FILE_NAME);
		PrintStream out = mTraceOut;
		out.println(label);
		for (String key : mMetricKeys) {
			out.print(key);
			out.print(',');
		}
		out.println();
		out.flush();
	}

	private void openTraceFile(String filename) {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File traceFileDir = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath()
					+ File.separator + MEMORY_TRACE_FILE_DIR);
			traceFileDir.mkdirs();
			File traceFile = new File(traceFileDir, filename);
			try {
				mTraceOut = new PrintStream(new FileOutputStream(traceFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.w(TAG, "Failed to open the trace file "+traceFile.getAbsolutePath());
			}			
		} else {
			Log.w(TAG, "Media not mounted");
		}
	}
	
	private static String getTraceFileName() {
		StringBuilder sb = new StringBuilder();
		sb.append(MEMORY_TRACE_FILE_NAME_PREFIX);
		sb.append(MEMORY_TRACE_FILE_NAME_DATE_FORMAT.format(new Date()));
		sb.append(MEMORY_TRACE_FILE_NAME_EXT);
		return sb.toString();
	}
	
	@Override
	public void writeTraceStop(Bundle results) {
		Log.d(TAG, "Stop tracing");
		closeTraceFile();		
	}

	private void closeTraceFile() {
		mTraceOut.close();
	}
	
	@Override
	public void writeTraceSnapshot(Bundle snapshot) {
		Log.d(TAG, "Write a snapshot "+snapshot);
		PrintStream out = mTraceOut;
		for (String key : mMetricKeys) {
			out.print(snapshot.get(key));
			out.print(',');
		}
		out.println();
		out.flush();
	}

}