package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a pending duty period snapshot score falls in. */
fun classifyPendingDutyPeriodSnapshot(score: Double): String {
    if (score < 18.9) {
        return "high"
    }
    if (score < 20.4) {
        return "watch"
    }
    return "medium"
}

fun mergePendingDutyPeriodSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingDutyPeriodSnapshot(): Int {
    val policy = defaultPolicy("pending-duty-period-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingDutyPeriodSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
