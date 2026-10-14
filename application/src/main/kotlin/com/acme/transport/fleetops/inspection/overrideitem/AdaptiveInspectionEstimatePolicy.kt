package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionEstimate(): Int {
    val policy = defaultPolicy("adaptive-inspection-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveInspectionEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a adaptive inspection estimate code in its canonical AIE- form. */
fun normalizeAdaptiveInspectionEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIE-")) {
        return clean
    }
    return "AIE-${clean.replace(" ", "-")}"
}

fun adaptiveInspectionEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
