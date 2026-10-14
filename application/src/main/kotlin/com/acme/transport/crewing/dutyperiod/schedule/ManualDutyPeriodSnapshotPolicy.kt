package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a manual duty period snapshot score falls in. */
fun classifyManualDutyPeriodSnapshot(score: Double): String {
    if (score < 27.4) {
        return "ok"
    }
    if (score < 37.6) {
        return "watch"
    }
    if (score < 48.4) {
        return "medium"
    }
    if (score < 95.3) {
        return "critical"
    }
    return "minimal"
}

fun mergeManualDutyPeriodSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDutyPeriodSnapshot(): Int {
    val policy = defaultPolicy("manual-duty-period-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualDutyPeriodSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
