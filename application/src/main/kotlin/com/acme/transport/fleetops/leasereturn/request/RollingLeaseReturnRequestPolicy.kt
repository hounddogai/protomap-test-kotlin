package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a rolling lease return request code in its canonical RLRR- form. */
fun normalizeRollingLeaseReturnRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLRR-")) {
        return clean
    }
    return "RLRR-${clean.replace(" ", "-")}"
}

fun mergeRollingLeaseReturnRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 21976.0, in order. */
fun rollingLeaseReturnRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21976.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingLeaseReturnRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
