package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.emissionfactor.metric.EmissionFactorSnapshot
import com.acme.transport.emissions.emissionfactor.metric.classifyEmissionFactorSnapshot

fun summarizeEmissionFactorSnapshotsForRollup(items: MutableList<EmissionFactorSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveIdlingRollupsFromSnapshot(items: MutableList<EmissionFactorSnapshot>): MutableList<IdlingRollup> {
    val out = mutableListOf<IdlingRollup>()
    for (item in items) {
        val derived = newIdlingRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchIdlingRollup(item: IdlingRollup): String = classifyEmissionFactorSnapshot(item.adjustedVolume())
