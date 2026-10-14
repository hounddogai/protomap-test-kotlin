package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.linehaul.capacity.assignment.RegionalCapacityAgreement
import com.acme.transport.linehaul.capacity.assignment.regionalCapacityAssignmentWeight

fun summarizeRegionalCapacityAgreementsForLedger(items: MutableList<RegionalCapacityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveAdaptiveBackhaulLedgersFromAgreement(items: MutableList<RegionalCapacityAgreement>): MutableList<AdaptiveBackhaulLedger> {
    val out = mutableListOf<AdaptiveBackhaulLedger>()
    for (item in items) {
        val derived = newAdaptiveBackhaulLedger(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveBackhaulLedger(item: AdaptiveBackhaulLedger): Double = regionalCapacityAssignmentWeight(8, true)
