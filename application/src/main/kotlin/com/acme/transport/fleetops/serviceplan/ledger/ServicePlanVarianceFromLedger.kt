package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.tire.draft.WeightedTireLedger
import com.acme.transport.fleetops.tire.draft.tireAdjustmentBucket

fun summarizeWeightedTireLedgersForVariance(items: MutableList<WeightedTireLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveServicePlanVariancesFromLedger(items: MutableList<WeightedTireLedger>): MutableList<ServicePlanVariance> {
    val out = mutableListOf<ServicePlanVariance>()
    for (item in items) {
        val derived = newServicePlanVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankServicePlanVariance(item: ServicePlanVariance): String = tireAdjustmentBucket(item.projectedExposure())
