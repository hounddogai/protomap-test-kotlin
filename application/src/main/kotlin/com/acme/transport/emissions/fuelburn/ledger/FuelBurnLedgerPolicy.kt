package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fuelBurnLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeFuelBurnLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fuel burn ledger score falls in. */
fun classifyFuelBurnLedger(score: Double): String {
    if (score < 5.0) {
        return "medium"
    }
    if (score < 18.1) {
        return "elevated"
    }
    if (score < 67.9) {
        return "low"
    }
    return "ok"
}

fun fuelBurnLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
