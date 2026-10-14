package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.emissions.evcharge.allocation.EvChargeDigest
import com.acme.transport.emissions.evcharge.allocation.evChargeEntryBucket

fun summarizeEvChargeDigestsForTarget(items: MutableList<EvChargeDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveEmissionFactorTargetsFromDigest(items: MutableList<EvChargeDigest>): MutableList<EmissionFactorTarget> {
    val out = mutableListOf<EmissionFactorTarget>()
    for (item in items) {
        val derived = newEmissionFactorTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveEmissionFactorTarget(item: EmissionFactorTarget): String = evChargeEntryBucket(item.effectiveValue())
