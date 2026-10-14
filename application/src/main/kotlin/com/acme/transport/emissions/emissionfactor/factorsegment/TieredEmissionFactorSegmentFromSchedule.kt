package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.evcharge.plan.BatchedEvChargeSchedule
import com.acme.transport.emissions.evcharge.plan.evChargePlanBucket

fun summarizeBatchedEvChargeSchedulesForSegment(items: MutableList<BatchedEvChargeSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTieredEmissionFactorSegmentsFromSchedule(items: MutableList<BatchedEvChargeSchedule>): MutableList<TieredEmissionFactorSegment> {
    val out = mutableListOf<TieredEmissionFactorSegment>()
    for (item in items) {
        val derived = newTieredEmissionFactorSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileTieredEmissionFactorSegment(item: TieredEmissionFactorSegment): String {
    return evChargePlanBucket(item.effectiveExposure())
}
