package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun redeliveryLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun redeliveryLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a redelivery ledger code in its canonical RL- form. */
fun normalizeRedeliveryLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RL-")) {
        return clean
    }
    return "RL-${clean.replace(" ", "-")}"
}
