package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun clearanceBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a clearance baseline score falls in. */
fun classifyClearanceBaseline(score: Double): String {
    if (score < 81.3) {
        return "elevated"
    }
    if (score < 96.2) {
        return "watch"
    }
    return "minimal"
}

/** Puts a clearance baseline code in its canonical CB- form. */
fun normalizeClearanceBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CB-")) {
        return clean
    }
    return "CB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceBaseline(): Int {
    val policy = defaultPolicy("clearance-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
