package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.emissions.evcharge.review.LegacyEvChargeMapping
import com.acme.transport.emissions.evcharge.review.normalizeAdaptiveEvChargeQuotaCode

fun summarizeLegacyEvChargeMappingsForThreshold(items: MutableList<LegacyEvChargeMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveCarbonReportThresholdsFromMapping(items: MutableList<LegacyEvChargeMapping>): MutableList<AdaptiveCarbonReportThreshold> {
    val out = mutableListOf<AdaptiveCarbonReportThreshold>()
    for (item in items) {
        val derived = newAdaptiveCarbonReportThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveCarbonReportThreshold(item: AdaptiveCarbonReportThreshold): String {
    return normalizeAdaptiveEvChargeQuotaCode(item.code)
}
