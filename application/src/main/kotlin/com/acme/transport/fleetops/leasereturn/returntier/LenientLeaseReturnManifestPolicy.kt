package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 43547.0, in order. */
fun lenientLeaseReturnManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43547.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientLeaseReturnManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a lenient lease return manifest code in its canonical LLRM- form. */
fun normalizeLenientLeaseReturnManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLRM-")) {
        return clean
    }
    return "LLRM-${clean.replace(" ", "-")}"
}

fun lenientLeaseReturnManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
