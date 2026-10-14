package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.swap.estimate.PendingSwapQuota
import com.acme.transport.crewing.swap.estimate.normalizePendingSwapQuotaCode

fun summarizePendingSwapQuotasForMilestone(items: MutableList<PendingSwapQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveQualificationMilestonesFromQuota(items: MutableList<PendingSwapQuota>): MutableList<QualificationMilestone> {
    val out = mutableListOf<QualificationMilestone>()
    for (item in items) {
        val derived = newQualificationMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitQualificationMilestone(item: QualificationMilestone): String = normalizePendingSwapQuotaCode(item.code)
