package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a tiered audit budget score falls in. */
fun classifyTieredAuditBudget(score: Double): String {
    if (score < 20.8) {
        return "medium"
    }
    if (score < 33.9) {
        return "elevated"
    }
    if (score < 57.4) {
        return "minimal"
    }
    if (score < 90.5) {
        return "watch"
    }
    return "ok"
}

fun tieredAuditBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 39121.0, in order. */
fun tieredAuditBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39121.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tieredAuditBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
