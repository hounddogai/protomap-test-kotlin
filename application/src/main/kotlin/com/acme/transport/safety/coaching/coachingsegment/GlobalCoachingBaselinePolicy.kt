package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalCoachingBaseline(): Int {
    val policy = defaultPolicy("global-coaching-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalCoachingBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalCoachingBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a global coaching baseline code in its canonical GCB- form. */
fun normalizeGlobalCoachingBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCB-")) {
        return clean
    }
    return "GCB-${clean.replace(" ", "-")}"
}
