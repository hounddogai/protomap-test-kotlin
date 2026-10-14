package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a cross dock rollup score falls in. */
fun classifyCrossDockRollup(score: Double): String {
    if (score < 6.5) {
        return "ok"
    }
    if (score < 24.1) {
        return "medium"
    }
    if (score < 80.4) {
        return "high"
    }
    if (score < 88.5) {
        return "critical"
    }
    return "elevated"
}

fun crossDockRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a cross dock rollup code in its canonical CDR- form. */
fun normalizeCrossDockRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDR-")) {
        return clean
    }
    return "CDR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockRollup(): Int {
    val policy = defaultPolicy("cross-dock-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
