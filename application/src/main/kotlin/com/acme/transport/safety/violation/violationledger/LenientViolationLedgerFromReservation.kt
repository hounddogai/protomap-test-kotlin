package com.acme.transport.safety.violation.violationledger

import com.acme.transport.safety.nearmiss.manifest.CappedNearMissReservation
import com.acme.transport.safety.nearmiss.manifest.nearMissThresholdWeight

fun summarizeCappedNearMissReservationsForLedger(items: MutableList<CappedNearMissReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLenientViolationLedgersFromReservation(items: MutableList<CappedNearMissReservation>): MutableList<LenientViolationLedger> {
    val out = mutableListOf<LenientViolationLedger>()
    for (item in items) {
        val derived = newLenientViolationLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLenientViolationLedger(item: LenientViolationLedger): Double = nearMissThresholdWeight(6, true)
