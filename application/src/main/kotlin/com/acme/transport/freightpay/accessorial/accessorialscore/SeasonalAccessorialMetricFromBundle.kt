package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.audit.tier.ScheduledAuditBundle
import com.acme.transport.freightpay.audit.tier.classifyRollingAuditThreshold

fun summarizeScheduledAuditBundlesForMetric(items: MutableList<ScheduledAuditBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalAccessorialMetricsFromBundle(items: MutableList<ScheduledAuditBundle>): MutableList<SeasonalAccessorialMetric> {
    val out = mutableListOf<SeasonalAccessorialMetric>()
    for (item in items) {
        val derived = newSeasonalAccessorialMetric(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSeasonalAccessorialMetric(item: SeasonalAccessorialMetric): String {
    return classifyRollingAuditThreshold(item.weightedExposure())
}
