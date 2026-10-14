package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a batched inspection schedule code in its canonical BIS- form. */
fun normalizeBatchedInspectionScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIS-")) {
        return clean
    }
    return "BIS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedInspectionSchedule(): Int {
    val policy = defaultPolicy("batched-inspection-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeBatchedInspectionScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a batched inspection schedule score falls in. */
fun classifyBatchedInspectionSchedule(score: Double): String {
    if (score < 56.2) {
        return "high"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "critical"
}
