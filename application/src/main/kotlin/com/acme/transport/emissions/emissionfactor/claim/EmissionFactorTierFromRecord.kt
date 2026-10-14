package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.evcharge.manifest.FallbackEvChargeRecord
import com.acme.transport.emissions.evcharge.manifest.manualEvChargeSegmentBucket

fun summarizeFallbackEvChargeRecordsForTier(items: MutableList<FallbackEvChargeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveEmissionFactorTiersFromRecord(items: MutableList<FallbackEvChargeRecord>): MutableList<EmissionFactorTier> {
    val out = mutableListOf<EmissionFactorTier>()
    for (item in items) {
        val derived = newEmissionFactorTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyEmissionFactorTierExtra(item: EmissionFactorTier): String {
    return manualEvChargeSegmentBucket(item.adjustedVolume())
}
