package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a sortation adjustment score falls in. */
fun classifySortationAdjustment(score: Double): String {
    if (score < 20.0) {
        return "ok"
    }
    if (score < 56.5) {
        return "high"
    }
    if (score < 92.8) {
        return "medium"
    }
    if (score < 99.2) {
        return "critical"
    }
    return "minimal"
}

/** Puts a sortation adjustment code in its canonical SA- form. */
fun normalizeSortationAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SA-")) {
        return clean
    }
    return "SA-${clean.replace(" ", "-")}"
}

fun sortationAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationAdjustment(): Int {
    val policy = defaultPolicy("sortation-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
