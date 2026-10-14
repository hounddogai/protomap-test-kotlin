package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.idling.target.IdlingTarget
import com.acme.transport.emissions.idling.target.classifyIdlingReservation

fun summarizeIdlingTargetsForSnapshot(items: MutableList<IdlingTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveEmissionFactorSnapshotsFromTarget(items: MutableList<IdlingTarget>): MutableList<EmissionFactorSnapshot> {
    val out = mutableListOf<EmissionFactorSnapshot>()
    for (item in items) {
        val derived = newEmissionFactorSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileEmissionFactorSnapshot(item: EmissionFactorSnapshot): String {
    return classifyIdlingReservation(item.projectedTotal())
}
