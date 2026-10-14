package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.qualification.proposal.LegacyQualificationMetric
import com.acme.transport.crewing.qualification.proposal.rollingQualificationNoticeBucket

fun summarizeLegacyQualificationMetricsForCycle(items: MutableList<LegacyQualificationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveSeasonalQualificationCyclesFromMetric(items: MutableList<LegacyQualificationMetric>): MutableList<SeasonalQualificationCycle> {
    val out = mutableListOf<SeasonalQualificationCycle>()
    for (item in items) {
        val derived = newSeasonalQualificationCycle(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileSeasonalQualificationCycle(item: SeasonalQualificationCycle): String {
    return rollingQualificationNoticeBucket(item.measuredValue())
}
