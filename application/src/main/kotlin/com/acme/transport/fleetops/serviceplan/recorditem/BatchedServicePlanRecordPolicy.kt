package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.numbers.roundTo

fun batchedServicePlanRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun batchedServicePlanRecordBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 38493.0, in order. */
fun batchedServicePlanRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38493.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched service plan record code in its canonical BSPR- form. */
fun normalizeBatchedServicePlanRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSPR-")) {
        return clean
    }
    return "BSPR-${clean.replace(" ", "-")}"
}
