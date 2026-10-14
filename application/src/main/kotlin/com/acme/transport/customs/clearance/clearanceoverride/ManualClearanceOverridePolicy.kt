package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a manual clearance override code in its canonical MCO- form. */
fun normalizeManualClearanceOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCO-")) {
        return clean
    }
    return "MCO-${clean.replace(" ", "-")}"
}

/** Names the band a manual clearance override score falls in. */
fun classifyManualClearanceOverride(score: Double): String {
    if (score < 20.1) {
        return "ok"
    }
    if (score < 34.1) {
        return "critical"
    }
    return "high"
}

fun manualClearanceOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualClearanceOverride(): Int {
    val policy = defaultPolicy("manual-clearance-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
