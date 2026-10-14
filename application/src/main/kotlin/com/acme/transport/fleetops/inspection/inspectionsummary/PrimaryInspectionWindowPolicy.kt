package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a primary inspection window score falls in. */
fun classifyPrimaryInspectionWindow(score: Double): String {
    if (score < 5.1) {
        return "watch"
    }
    if (score < 19.8) {
        return "ok"
    }
    if (score < 55.4) {
        return "low"
    }
    return "critical"
}

fun primaryInspectionWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun primaryInspectionWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryInspectionWindow(): Int {
    val policy = defaultPolicy("primary-inspection-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
