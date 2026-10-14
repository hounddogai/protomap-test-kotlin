package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.tire.batch.RegionalTireBatch
import com.acme.transport.fleetops.tire.batch.classifyGlobalTireSchedule

fun summarizeRegionalTireBatchesForManifest(items: MutableList<RegionalTireBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedServicePlanManifestsFromBatch(items: MutableList<RegionalTireBatch>): MutableList<WeightedServicePlanManifest> {
    val out = mutableListOf<WeightedServicePlanManifest>()
    for (item in items) {
        val derived = newWeightedServicePlanManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreWeightedServicePlanManifest(item: WeightedServicePlanManifest): String {
    return classifyGlobalTireSchedule(item.weightedLoad())
}
