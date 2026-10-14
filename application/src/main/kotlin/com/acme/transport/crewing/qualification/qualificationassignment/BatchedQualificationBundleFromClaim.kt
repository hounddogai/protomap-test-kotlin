package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.dutyperiod.periodassignment.WeightedDutyPeriodClaim
import com.acme.transport.crewing.dutyperiod.periodassignment.normalizePendingDutyPeriodTemplateCode

fun summarizeWeightedDutyPeriodClaimsForBundle(items: MutableList<WeightedDutyPeriodClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveBatchedQualificationBundlesFromClaim(items: MutableList<WeightedDutyPeriodClaim>): MutableList<BatchedQualificationBundle> {
    val out = mutableListOf<BatchedQualificationBundle>()
    for (item in items) {
        val derived = newBatchedQualificationBundle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planBatchedQualificationBundle(item: BatchedQualificationBundle): String {
    return normalizePendingDutyPeriodTemplateCode(item.code)
}
