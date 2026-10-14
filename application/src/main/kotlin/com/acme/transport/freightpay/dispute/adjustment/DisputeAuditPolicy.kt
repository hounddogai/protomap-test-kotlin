package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a dispute audit code in its canonical DA- form. */
fun normalizeDisputeAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}

fun disputeAuditBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 70433.0, in order. */
fun disputeAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70433.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun disputeAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
