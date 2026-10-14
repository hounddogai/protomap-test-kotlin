package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalAuditMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 87398.0, in order. */
fun globalAuditMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87398.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalAuditMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
