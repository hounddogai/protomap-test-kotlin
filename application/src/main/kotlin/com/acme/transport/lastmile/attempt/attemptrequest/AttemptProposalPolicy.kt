package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun attemptProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun attemptProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 61541.0, in order. */
fun attemptProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61541.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a attempt proposal code in its canonical AP- form. */
fun normalizeAttemptProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AP-")) {
        return clean
    }
    return "AP-${clean.replace(" ", "-")}"
}
