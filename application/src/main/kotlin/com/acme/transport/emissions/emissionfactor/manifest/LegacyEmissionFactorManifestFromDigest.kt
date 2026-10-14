package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.fuelburn.baseline.FuelBurnDigest
import com.acme.transport.emissions.fuelburn.baseline.fuelBurnOverrideWeight

fun summarizeFuelBurnDigestsForManifest(items: MutableList<FuelBurnDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyEmissionFactorManifestsFromDigest(items: MutableList<FuelBurnDigest>): MutableList<LegacyEmissionFactorManifest> {
    val out = mutableListOf<LegacyEmissionFactorManifest>()
    for (item in items) {
        val derived = newLegacyEmissionFactorManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLegacyEmissionFactorManifest(item: LegacyEmissionFactorManifest): Double {
    return fuelBurnOverrideWeight(6, true)
}
