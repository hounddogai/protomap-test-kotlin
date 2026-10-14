package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.emissionfactor.entry.RollingEmissionFactorQuota
import com.acme.transport.emissions.emissionfactor.entry.pendingEmissionFactorClaimBucket

fun summarizeRollingEmissionFactorQuotasForMetric(items: MutableList<RollingEmissionFactorQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun derivePrimaryFuelBurnMetricsFromQuota(items: MutableList<RollingEmissionFactorQuota>): MutableList<PrimaryFuelBurnMetric> {
    val out = mutableListOf<PrimaryFuelBurnMetric>()
    for (item in items) {
        val derived = newPrimaryFuelBurnMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scorePrimaryFuelBurnMetric(item: PrimaryFuelBurnMetric): String {
    return pendingEmissionFactorClaimBucket(item.effectiveExposure())
}
