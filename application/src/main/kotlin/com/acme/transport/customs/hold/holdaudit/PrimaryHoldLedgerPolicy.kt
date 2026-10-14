package com.acme.transport.customs.hold.holdaudit

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryHoldLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun primaryHoldLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a primary hold ledger code in its canonical PHL- form. */
fun normalizePrimaryHoldLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PHL-")) {
        return clean
    }
    return "PHL-${clean.replace(" ", "-")}"
}

/** Names the band a primary hold ledger score falls in. */
fun classifyPrimaryHoldLedger(score: Double): String {
    if (score < 26.2) {
        return "elevated"
    }
    if (score < 39.4) {
        return "ok"
    }
    if (score < 45.6) {
        return "medium"
    }
    if (score < 67.6) {
        return "critical"
    }
    return "watch"
}
