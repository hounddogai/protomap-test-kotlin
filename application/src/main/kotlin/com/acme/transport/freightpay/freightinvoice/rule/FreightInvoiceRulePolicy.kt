package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a freight invoice rule score falls in. */
fun classifyFreightInvoiceRule(score: Double): String {
    if (score < 11.2) {
        return "high"
    }
    if (score < 70.2) {
        return "low"
    }
    if (score < 70.9) {
        return "critical"
    }
    if (score < 83.8) {
        return "watch"
    }
    return "ok"
}

fun freightInvoiceRuleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun freightInvoiceRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFreightInvoiceRule(): Int {
    val policy = defaultPolicy("freight-invoice-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
