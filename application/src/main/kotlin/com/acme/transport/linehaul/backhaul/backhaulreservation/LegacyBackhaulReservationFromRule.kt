package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.linehaul.capacity.milestone.CapacityRule
import com.acme.transport.linehaul.capacity.milestone.classifyCapacityRule

fun summarizeCapacityRulesForReservation(items: MutableList<CapacityRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLegacyBackhaulReservationsFromRule(items: MutableList<CapacityRule>): MutableList<LegacyBackhaulReservation> {
    val out = mutableListOf<LegacyBackhaulReservation>()
    for (item in items) {
        val derived = newLegacyBackhaulReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyBackhaulReservation(item: LegacyBackhaulReservation): String {
    return classifyCapacityRule(item.projectedVolume())
}
