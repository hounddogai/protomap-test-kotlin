package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.numbers.roundTo

fun pendingLockerAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun pendingLockerAuditBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 51639.0, in order. */
fun pendingLockerAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51639.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending locker audit score falls in. */
fun classifyPendingLockerAudit(score: Double): String {
    if (score < 3.2) {
        return "ok"
    }
    if (score < 37.9) {
        return "watch"
    }
    if (score < 74.1) {
        return "minimal"
    }
    if (score < 78.0) {
        return "critical"
    }
    return "medium"
}
