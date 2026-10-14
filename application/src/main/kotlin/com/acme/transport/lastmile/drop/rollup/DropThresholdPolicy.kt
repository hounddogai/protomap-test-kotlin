package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun dropThresholdBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a drop threshold code in its canonical DT- form. */
fun normalizeDropThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DT-")) {
        return clean
    }
    return "DT-${clean.replace(" ", "-")}"
}

/** Names the band a drop threshold score falls in. */
fun classifyDropThreshold(score: Double): String {
    if (score < 88.0) {
        return "watch"
    }
    if (score < 93.3) {
        return "ok"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropThreshold(): Int {
    val policy = defaultPolicy("drop-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
