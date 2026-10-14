package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 75820.0, in order. */
fun seasonalRedeliveryAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75820.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRedeliveryAudit(): Int {
    val policy = defaultPolicy("seasonal-redelivery-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun seasonalRedeliveryAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a seasonal redelivery audit code in its canonical SRA- form. */
fun normalizeSeasonalRedeliveryAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRA-")) {
        return clean
    }
    return "SRA-${clean.replace(" ", "-")}"
}
