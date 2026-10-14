package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingServiceAreaAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingServiceAreaAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a pending service area assignment code in its canonical PSAA- form. */
fun normalizePendingServiceAreaAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSAA-")) {
        return clean
    }
    return "PSAA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 91480.0, in order. */
fun pendingServiceAreaAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91480.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
