package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a incident ledger score falls in. */
fun classifyIncidentLedger(score: Double): String {
    if (score < 4.2) {
        return "elevated"
    }
    if (score < 26.9) {
        return "high"
    }
    if (score < 54.2) {
        return "medium"
    }
    if (score < 65.6) {
        return "minimal"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 15592.0, in order. */
fun incidentLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15592.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun incidentLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeIncidentLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
