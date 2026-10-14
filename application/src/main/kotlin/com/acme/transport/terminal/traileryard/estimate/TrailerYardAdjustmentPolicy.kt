package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a trailer yard adjustment code in its canonical TYA- form. */
fun normalizeTrailerYardAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYA-")) {
        return clean
    }
    return "TYA-${clean.replace(" ", "-")}"
}

fun trailerYardAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardAdjustment(): Int {
    val policy = defaultPolicy("trailer-yard-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
