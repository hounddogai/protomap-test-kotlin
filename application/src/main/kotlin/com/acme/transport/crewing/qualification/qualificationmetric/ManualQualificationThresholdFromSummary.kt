package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.dutyperiod.summary.DutyPeriodSummary
import com.acme.transport.crewing.dutyperiod.summary.defaultDutyPeriodAssignmentBucket

fun summarizeDutyPeriodSummariesForThreshold(items: MutableList<DutyPeriodSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveManualQualificationThresholdsFromSummary(items: MutableList<DutyPeriodSummary>): MutableList<ManualQualificationThreshold> {
    val out = mutableListOf<ManualQualificationThreshold>()
    for (item in items) {
        val derived = newManualQualificationThreshold(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreManualQualificationThreshold(item: ManualQualificationThreshold): String {
    return defaultDutyPeriodAssignmentBucket(item.netTotal())
}
