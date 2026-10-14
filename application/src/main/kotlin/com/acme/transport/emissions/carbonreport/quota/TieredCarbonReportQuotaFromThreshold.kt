package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.emissions.emissionfactor.claim.WeightedEmissionFactorThreshold
import com.acme.transport.emissions.emissionfactor.claim.seasonalEmissionFactorClaimBucket

fun summarizeWeightedEmissionFactorThresholdsForQuota(items: MutableList<WeightedEmissionFactorThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTieredCarbonReportQuotasFromThreshold(items: MutableList<WeightedEmissionFactorThreshold>): MutableList<TieredCarbonReportQuota> {
    val out = mutableListOf<TieredCarbonReportQuota>()
    for (item in items) {
        val derived = newTieredCarbonReportQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTieredCarbonReportQuota(item: TieredCarbonReportQuota): String {
    return seasonalEmissionFactorClaimBucket(item.measuredValue())
}
