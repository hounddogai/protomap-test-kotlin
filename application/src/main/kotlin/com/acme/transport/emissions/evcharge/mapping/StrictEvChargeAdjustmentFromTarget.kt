package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.emissionfactor.mapping.PendingEmissionFactorTarget
import com.acme.transport.emissions.emissionfactor.mapping.adaptiveEmissionFactorMappingBucket

fun summarizePendingEmissionFactorTargetsForAdjustment(items: MutableList<PendingEmissionFactorTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveStrictEvChargeAdjustmentsFromTarget(items: MutableList<PendingEmissionFactorTarget>): MutableList<StrictEvChargeAdjustment> {
    val out = mutableListOf<StrictEvChargeAdjustment>()
    for (item in items) {
        val derived = newStrictEvChargeAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectStrictEvChargeAdjustment(item: StrictEvChargeAdjustment): String {
    return adaptiveEmissionFactorMappingBucket(item.projectedValue())
}
