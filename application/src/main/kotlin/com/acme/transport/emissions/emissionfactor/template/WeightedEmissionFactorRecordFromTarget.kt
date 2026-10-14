package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.idling.idlingtarget.CappedIdlingTarget
import com.acme.transport.emissions.idling.idlingtarget.seasonalIdlingProfileBucket

fun summarizeCappedIdlingTargetsForRecord(items: MutableList<CappedIdlingTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveWeightedEmissionFactorRecordsFromTarget(items: MutableList<CappedIdlingTarget>): MutableList<WeightedEmissionFactorRecord> {
    val out = mutableListOf<WeightedEmissionFactorRecord>()
    for (item in items) {
        val derived = newWeightedEmissionFactorRecord(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankWeightedEmissionFactorRecord(item: WeightedEmissionFactorRecord): String {
    return seasonalIdlingProfileBucket(item.projectedExposure())
}
