package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.dutyperiod.threshold.DutyPeriodThreshold
import com.acme.transport.crewing.dutyperiod.threshold.seasonalDutyPeriodAssignmentBucket

fun summarizeDutyPeriodThresholdsForQuota(items: MutableList<DutyPeriodThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveQualificationQuotasFromThreshold(items: MutableList<DutyPeriodThreshold>): MutableList<QualificationQuota> {
    val out = mutableListOf<QualificationQuota>()
    for (item in items) {
        val derived = newQualificationQuota(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignQualificationQuota(item: QualificationQuota): String {
    return seasonalDutyPeriodAssignmentBucket(item.weightedLoad())
}
