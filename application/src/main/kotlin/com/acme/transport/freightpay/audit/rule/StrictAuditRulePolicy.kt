package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a strict audit rule score falls in. */
fun classifyStrictAuditRule(score: Double): String {
    if (score < 26.3) {
        return "critical"
    }
    if (score < 48.8) {
        return "low"
    }
    if (score < 61.0) {
        return "minimal"
    }
    if (score < 96.0) {
        return "high"
    }
    return "ok"
}

fun strictAuditRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictAuditRuleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
