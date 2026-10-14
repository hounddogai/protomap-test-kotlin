package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualBackhaulOverride(): Int {
    val policy = defaultPolicy("manual-backhaul-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualBackhaulOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual backhaul override code in its canonical MBO- form. */
fun normalizeManualBackhaulOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MBO-")) {
        return clean
    }
    return "MBO-${clean.replace(" ", "-")}"
}
