package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.crewing.qualification.preference.PendingQualificationMetric
import com.acme.transport.crewing.qualification.preference.normalizeQualificationPreferenceCode

fun summarizePendingQualificationMetricsForBudget(items: MutableList<PendingQualificationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveCappedAvailabilityBudgetsFromMetric(items: MutableList<PendingQualificationMetric>): MutableList<CappedAvailabilityBudget> {
    val out = mutableListOf<CappedAvailabilityBudget>()
    for (item in items) {
        val derived = newCappedAvailabilityBudget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeCappedAvailabilityBudget(item: CappedAvailabilityBudget): String {
    return normalizeQualificationPreferenceCode(item.code)
}
