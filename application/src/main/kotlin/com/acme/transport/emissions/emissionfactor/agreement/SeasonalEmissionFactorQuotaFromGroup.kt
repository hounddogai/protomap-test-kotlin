package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.emissions.carbonreport.threshold.AdaptiveCarbonReportGroup
import com.acme.transport.emissions.carbonreport.threshold.carbonReportThresholdBucket

fun summarizeAdaptiveCarbonReportGroupsForQuota(items: MutableList<AdaptiveCarbonReportGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveSeasonalEmissionFactorQuotasFromGroup(items: MutableList<AdaptiveCarbonReportGroup>): MutableList<SeasonalEmissionFactorQuota> {
    val out = mutableListOf<SeasonalEmissionFactorQuota>()
    for (item in items) {
        val derived = newSeasonalEmissionFactorQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalEmissionFactorQuota(item: SeasonalEmissionFactorQuota): String {
    return carbonReportThresholdBucket(item.measuredValue())
}
