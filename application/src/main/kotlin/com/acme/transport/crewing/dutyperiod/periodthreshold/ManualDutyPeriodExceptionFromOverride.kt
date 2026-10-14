package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.qualification.summary.PrimaryQualificationOverride
import com.acme.transport.crewing.qualification.summary.primaryQualificationOverrideBucket

fun summarizePrimaryQualificationOverridesForException(items: MutableList<PrimaryQualificationOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveManualDutyPeriodExceptionsFromOverride(items: MutableList<PrimaryQualificationOverride>): MutableList<ManualDutyPeriodException> {
    val out = mutableListOf<ManualDutyPeriodException>()
    for (item in items) {
        val derived = newManualDutyPeriodException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleManualDutyPeriodException(item: ManualDutyPeriodException): String {
    return primaryQualificationOverrideBucket(item.projectedExposure())
}
