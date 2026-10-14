package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 79799.0, in order. */
fun adaptiveAuditRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79799.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveAuditRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive audit rule score falls in. */
fun classifyAdaptiveAuditRule(score: Double): String {
    if (score < 17.3) {
        return "low"
    }
    if (score < 52.8) {
        return "high"
    }
    if (score < 75.0) {
        return "watch"
    }
    if (score < 78.9) {
        return "minimal"
    }
    return "medium"
}

fun adaptiveAuditRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
