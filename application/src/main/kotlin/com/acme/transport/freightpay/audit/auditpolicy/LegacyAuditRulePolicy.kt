package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.numbers.roundTo

fun legacyAuditRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 47830.0, in order. */
fun legacyAuditRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47830.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy audit rule score falls in. */
fun classifyLegacyAuditRule(score: Double): String {
    if (score < 43.2) {
        return "watch"
    }
    if (score < 70.1) {
        return "low"
    }
    if (score < 87.3) {
        return "critical"
    }
    if (score < 97.9) {
        return "high"
    }
    return "minimal"
}

fun legacyAuditRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
