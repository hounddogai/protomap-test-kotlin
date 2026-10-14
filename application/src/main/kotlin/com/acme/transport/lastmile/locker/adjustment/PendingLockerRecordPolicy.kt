package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.numbers.roundTo

fun pendingLockerRecordBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a pending locker record score falls in. */
fun classifyPendingLockerRecord(score: Double): String {
    if (score < 22.8) {
        return "watch"
    }
    if (score < 60.4) {
        return "ok"
    }
    return "low"
}

/** Puts a pending locker record code in its canonical PLR- form. */
fun normalizePendingLockerRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLR-")) {
        return clean
    }
    return "PLR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 66670.0, in order. */
fun pendingLockerRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66670.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
