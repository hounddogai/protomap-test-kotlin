package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyClearanceScore(): Int {
    val policy = defaultPolicy("legacy-clearance-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyClearanceScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a legacy clearance score code in its canonical LCS- form. */
fun normalizeLegacyClearanceScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCS-")) {
        return clean
    }
    return "LCS-${clean.replace(" ", "-")}"
}

fun legacyClearanceScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
