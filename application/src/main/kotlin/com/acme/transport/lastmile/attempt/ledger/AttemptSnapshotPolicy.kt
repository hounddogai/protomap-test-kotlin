package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a attempt snapshot code in its canonical AS- form. */
fun normalizeAttemptSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

fun attemptSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 51294.0, in order. */
fun attemptSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51294.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun attemptSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
