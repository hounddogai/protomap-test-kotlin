package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.evcharge.regionalchargeslot.EvChargeScore
import com.acme.transport.emissions.evcharge.regionalchargeslot.evChargeScoreWeight

fun summarizeEvChargeScoresForProfile(items: MutableList<EvChargeScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveEmissionFactorProfilesFromScore(items: MutableList<EvChargeScore>): MutableList<EmissionFactorProfile> {
    val out = mutableListOf<EmissionFactorProfile>()
    for (item in items) {
        val derived = newEmissionFactorProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignEmissionFactorProfile(item: EmissionFactorProfile): Double = evChargeScoreWeight(2, true)
