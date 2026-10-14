package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a rate card rule code in its canonical RCR- form. */
fun normalizeRateCardRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}

fun rateCardRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rateCardRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardRule(): Int {
    val policy = defaultPolicy("rate-card-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
