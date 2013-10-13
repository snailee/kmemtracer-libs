package org.kandroid.memtracer;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Debug;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;

/**
 * Note that most part of this class is taken from android.os.PerformanceCollector.
 * 
 */
public class MemoryTracer {

    public interface ResultsWriter {
        public void writeTraceStart(String label);
        public void writeTraceStop(Bundle results);
        public void writeTraceSnapshot(Bundle snapshot);
    }

    /**
     * In a results Bundle, this key references a list of snapshot Bundles.
     */
    public static final String METRIC_KEY_SNAPSHOTS = "snapshots";
    /**
     * In a snapshot Bundle, this key describes the snapshot.
     */
    public static final String METRIC_KEY_LABEL = "label";
    /**
     * In a results Bundle, this key reports the cpu time of the code block
     * under measurement.
     */
    public static final String METRIC_KEY_CPU_TIME = "cpu_time";
    /**
     * In a results Bundle, this key reports the execution time of the code
     * block under measurement.
     */
    public static final String METRIC_KEY_EXECUTION_TIME = "execution_time";
    /**
     * In a snapshot Bundle, this key reports the number of received
     * transactions from the binder driver before collection started.
     */
    public static final String METRIC_KEY_PRE_RECEIVED_TRANSACTIONS = "pre_received_transactions";
    /**
     * In a snapshot Bundle, this key reports the number of transactions sent by
     * the running program before collection started.
     */
    public static final String METRIC_KEY_PRE_SENT_TRANSACTIONS = "pre_sent_transactions";
    /**
     * In a snapshot Bundle, this key reports the number of received
     * transactions from the binder driver.
     */
    public static final String METRIC_KEY_RECEIVED_TRANSACTIONS = "received_transactions";
    /**
     * In a snapshot Bundle, this key reports the number of transactions sent by
     * the running program.
     */
    public static final String METRIC_KEY_SENT_TRANSACTIONS = "sent_transactions";
    /**
     * In a snapshot Bundle, this key reports the number of garbage collection
     * invocations.
     */
    public static final String METRIC_KEY_GC_INVOCATION_COUNT = "gc_invocation_count";
    /**
     * In a snapshot Bundle, this key reports the amount of allocated memory
     * used by the running program.
     */
    public static final String METRIC_KEY_JAVA_ALLOCATED = "java_allocated";
    /**
     * In a snapshot Bundle, this key reports the amount of free memory
     * available to the running program.
     */
    public static final String METRIC_KEY_JAVA_FREE = "java_free";
    /**
     * In a snapshot Bundle, this key reports the number of private dirty pages
     * used by dalvik.
     */
    public static final String METRIC_KEY_JAVA_PRIVATE_DIRTY = "java_private_dirty";
    /**
     * In a snapshot Bundle, this key reports the proportional set size for
     * dalvik.
     */
    public static final String METRIC_KEY_JAVA_PSS = "java_pss";
    /**
     * In a snapshot Bundle, this key reports the number of shared dirty pages
     * used by dalvik.
     */
    public static final String METRIC_KEY_JAVA_SHARED_DIRTY = "java_shared_dirty";
    /**
     * In a snapshot Bundle, this key reports the total amount of memory
     * available to the running program.
     */
    public static final String METRIC_KEY_JAVA_SIZE = "java_size";
    /**
     * In a snapshot Bundle, this key reports the amount of allocated memory in
     * the native heap.
     */
    public static final String METRIC_KEY_NATIVE_ALLOCATED = "native_allocated";
    /**
     * In a snapshot Bundle, this key reports the amount of free memory in the
     * native heap.
     */
    public static final String METRIC_KEY_NATIVE_FREE = "native_free";
    /**
     * In a snapshot Bundle, this key reports the number of private dirty pages
     * used by the native heap.
     */
    public static final String METRIC_KEY_NATIVE_PRIVATE_DIRTY = "native_private_dirty";
    /**
     * In a snapshot Bundle, this key reports the proportional set size for the
     * native heap.
     */
    public static final String METRIC_KEY_NATIVE_PSS = "native_pss";
    /**
     * In a snapshot Bundle, this key reports the number of shared dirty pages
     * used by the native heap.
     */
    public static final String METRIC_KEY_NATIVE_SHARED_DIRTY = "native_shared_dirty";
    /**
     * In a snapshot Bundle, this key reports the size of the native heap.
     */
    public static final String METRIC_KEY_NATIVE_SIZE = "native_size";
    /**
     * In a snapshot Bundle, this key reports the number of objects allocated
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_ALLOC_COUNT = "global_alloc_count";
    /**
     * In a snapshot Bundle, this key reports the size of all objects allocated
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_ALLOC_SIZE = "global_alloc_size";
    /**
     * In a snapshot Bundle, this key reports the number of objects freed
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_FREED_COUNT = "global_freed_count";
    /**
     * In a snapshot Bundle, this key reports the size of all objects freed
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_FREED_SIZE = "global_freed_size";
    /**
     * In a snapshot Bundle, this key reports the number of objects allocated
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_EXTERNAL_ALLOC_COUNT = "global_external_alloc_count";
    /**
     * In a snapshot Bundle, this key reports the size of all objects allocated
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_EXTERNAL_ALLOC_SIZE = "global_external_alloc_size";
    /**
     * In a snapshot Bundle, this key reports the number of objects freed
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_EXTERNAL_FREED_COUNT = "global_external_freed_count";
    /**
     * In a snapshot Bundle, this key reports the size of all objects freed
     * globally.
     */
    public static final String METRIC_KEY_GLOBAL_EXTERNAL_FREED_SIZE = "global_external_freed_size";
    /**
     * In a snapshot Bundle, this key reports the number of private dirty pages
     * used by everything else.
     */
    public static final String METRIC_KEY_OTHER_PRIVATE_DIRTY = "other_private_dirty";
    /**
     * In a snapshot Bundle, this key reports the proportional set size for
     * everything else.
     */
    public static final String METRIC_KEY_OTHER_PSS = "other_pss";
    /**
     * In a snapshot Bundle, this key reports the number of shared dirty pages
     * used by everything else.
     */
    public static final String METRIC_KEY_OTHER_SHARED_DIRTY = "other_shared_dirty";

    private ResultsWriter mResultsWriter;
    private Bundle mPerfResults;
    private long mSnapshotCpuTime;
    private long mSnapshotExecTime;
    private boolean mShouldReportResults;
	private boolean mIsStarted;
    
    public MemoryTracer() {
    	
    }

    public MemoryTracer(ResultsWriter writer) {
        setResultsWriter(writer);
    }

    public void setResultsWriter(ResultsWriter writer) {
        mResultsWriter = writer;
    }

    public synchronized void startTracing(String label) {
		if (mIsStarted) {
			return;
		}
		mIsStarted = true;
		
        if (mResultsWriter != null)
            mResultsWriter.writeTraceStart(label);
        startPerformanceTracking();
    }

    public synchronized Bundle stopTracing() {
		if (!mIsStarted) {
			// TODO: Should return an empty bundle? 
			return null;
		}
		mIsStarted = false;
		
    	if (mResultsWriter != null)
            mResultsWriter.writeTraceStop(mPerfResults);
    	stopPerformanceTracking();
    	return mPerfResults;
    }
    
    public Bundle addSnapshot(String label) {
        // Stop the timing. This must be done first before any other counting is stopped.
        mSnapshotCpuTime = Process.getElapsedCpuTime() - mSnapshotCpuTime;
        mSnapshotExecTime = SystemClock.uptimeMillis() - mSnapshotExecTime;

        resetAllocCounting();

        long nativeMax = Debug.getNativeHeapSize() / 1024;
        long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024;
        long nativeFree = Debug.getNativeHeapFreeSize() / 1024;

        Debug.MemoryInfo memInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memInfo);

        Runtime runtime = Runtime.getRuntime();

        long dalvikMax = runtime.totalMemory() / 1024;
        long dalvikFree = runtime.freeMemory() / 1024;
        long dalvikAllocated = dalvikMax - dalvikFree;

        Bundle snapshot = new Bundle();
        snapshot.putString(METRIC_KEY_LABEL, label);
        
        // Add final binder counts
        Bundle binderCounts = getBinderCounts();
        for (String key : binderCounts.keySet()) {
        	snapshot.putLong(key, binderCounts.getLong(key));
        }

        // Add alloc counts
        Bundle allocCounts = getAllocCounts();
        for (String key : allocCounts.keySet()) {
        	snapshot.putLong(key, allocCounts.getLong(key));
        }

        snapshot.putLong(METRIC_KEY_EXECUTION_TIME, mSnapshotExecTime);
        snapshot.putLong(METRIC_KEY_CPU_TIME, mSnapshotCpuTime);

        snapshot.putLong(METRIC_KEY_NATIVE_SIZE, nativeMax);
        snapshot.putLong(METRIC_KEY_NATIVE_ALLOCATED, nativeAllocated);
        snapshot.putLong(METRIC_KEY_NATIVE_FREE, nativeFree);
        snapshot.putLong(METRIC_KEY_NATIVE_PSS, memInfo.nativePss);
        snapshot.putLong(METRIC_KEY_NATIVE_PRIVATE_DIRTY, memInfo.nativePrivateDirty);
        snapshot.putLong(METRIC_KEY_NATIVE_SHARED_DIRTY, memInfo.nativeSharedDirty);

        snapshot.putLong(METRIC_KEY_JAVA_SIZE, dalvikMax);
        snapshot.putLong(METRIC_KEY_JAVA_ALLOCATED, dalvikAllocated);
        snapshot.putLong(METRIC_KEY_JAVA_FREE, dalvikFree);
        snapshot.putLong(METRIC_KEY_JAVA_PSS, memInfo.dalvikPss);
        snapshot.putLong(METRIC_KEY_JAVA_PRIVATE_DIRTY, memInfo.dalvikPrivateDirty);
        snapshot.putLong(METRIC_KEY_JAVA_SHARED_DIRTY, memInfo.dalvikSharedDirty);

        snapshot.putLong(METRIC_KEY_OTHER_PSS, memInfo.otherPss);
        snapshot.putLong(METRIC_KEY_OTHER_PRIVATE_DIRTY, memInfo.otherPrivateDirty);
        snapshot.putLong(METRIC_KEY_OTHER_SHARED_DIRTY, memInfo.otherSharedDirty);

        if (mShouldReportResults) {
        	mPerfResults.getParcelableArrayList(METRIC_KEY_SNAPSHOTS).add(snapshot);
        }

        if (mResultsWriter != null) {
            mResultsWriter.writeTraceSnapshot(snapshot);
        }
        
        return snapshot;
    }

    /*
     * Starts tracking memory usage, binder transactions, and real & cpu timing.
     */
    private void startPerformanceTracking() {
        // Create new snapshot
        mPerfResults = new Bundle();
        mPerfResults.putParcelableArrayList(
                METRIC_KEY_SNAPSHOTS, new ArrayList<Parcelable>());

        // Add initial binder counts
        Bundle binderCounts = getBinderCounts();
        for (String key : binderCounts.keySet()) {
            mPerfResults.putLong("pre_" + key, binderCounts.getLong(key));
        }

        // Force a GC and zero out the performance counters. Do this
        // before reading initial CPU/wall-clock times so we don't include
        // the cost of this setup in our final metrics.
        startAllocCounting();

        // Record CPU time up to this point, and start timing. Note: this
        // must happen at the end of this method, otherwise the timing will
        // include noise.
        mSnapshotExecTime = SystemClock.uptimeMillis();
        mSnapshotCpuTime = Process.getElapsedCpuTime();
    }

    /*
     * Stops tracking memory usage, binder transactions, and real & cpu timing.
     * Stores collected data as type long into Bundle object for reporting.
     */
    private void stopPerformanceTracking() {
        // Stop the timing. This must be done first before any other counting is
        // stopped.
        mSnapshotCpuTime = Process.getElapsedCpuTime() - mSnapshotCpuTime;
        mSnapshotExecTime = SystemClock.uptimeMillis() - mSnapshotExecTime;

        stopAllocCounting();
    }

    /*
     * Starts allocation counting. This triggers a gc and resets the counts.
     */
    private static void startAllocCounting() {
        // Before we start trigger a GC and reset the debug counts. Run the
        // finalizers and another GC before starting and stopping the alloc
        // counts. This will free up any objects that were just sitting around
        // waiting for their finalizers to be run.
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();

        Debug.resetAllCounts();

        // start the counts
        Debug.startAllocCounting();
    }

    private static void resetAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();

        Debug.resetAllCounts();    	
    }
    
    /*
     * Stops allocation counting.
     */
    private static void stopAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();

        Debug.stopAllocCounting();
    }

    /*
     * Returns a bundle with the current results from the allocation counting.
     */
    private static Bundle getAllocCounts() {
        Bundle results = new Bundle();
        results.putLong(METRIC_KEY_GLOBAL_ALLOC_COUNT, Debug.getGlobalAllocCount());
        results.putLong(METRIC_KEY_GLOBAL_ALLOC_SIZE, Debug.getGlobalAllocSize());
        results.putLong(METRIC_KEY_GLOBAL_FREED_COUNT, Debug.getGlobalFreedCount());
        results.putLong(METRIC_KEY_GLOBAL_FREED_SIZE, Debug.getGlobalFreedSize());
        results.putLong(METRIC_KEY_GLOBAL_EXTERNAL_ALLOC_COUNT, Debug.getGlobalExternalAllocCount());
        results.putLong(METRIC_KEY_GLOBAL_EXTERNAL_ALLOC_SIZE, Debug.getGlobalExternalAllocSize());
        results.putLong(METRIC_KEY_GLOBAL_EXTERNAL_FREED_COUNT, Debug.getGlobalExternalFreedCount());
        results.putLong(METRIC_KEY_GLOBAL_EXTERNAL_FREED_SIZE, Debug.getGlobalExternalFreedSize());
        results.putLong(METRIC_KEY_GC_INVOCATION_COUNT, Debug.getGlobalGcInvocationCount());
        return results;
    }

    /*
     * Returns a bundle with the counts for various binder counts for this
     * process. Currently the only two that are reported are the number of send
     * and the number of received transactions.
     */
    private static Bundle getBinderCounts() {
        Bundle results = new Bundle();
        results.putLong(METRIC_KEY_SENT_TRANSACTIONS, Debug.getBinderSentTransactions());
        results.putLong(METRIC_KEY_RECEIVED_TRANSACTIONS, Debug.getBinderReceivedTransactions());
        return results;
    }
}
