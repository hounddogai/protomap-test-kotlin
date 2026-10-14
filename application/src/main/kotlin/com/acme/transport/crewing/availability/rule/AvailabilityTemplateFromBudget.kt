package com.acme.transport.crewing.availability.rule

import com.acme.transport.crewing.availability.forecast.AvailabilityBudget
import com.acme.transport.crewing.availability.forecast.classifyAvailabilityNotice

fun summarizeAvailabilityBudgetsForTemplate(items: MutableList<AvailabilityBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAvailabilityTemplatesFromBudget(items: MutableList<AvailabilityBudget>): MutableList<AvailabilityTemplate> {
    val out = mutableListOf<AvailabilityTemplate>()
    for (item in items) {
        val derived = newAvailabilityTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileAvailabilityTemplate(item: AvailabilityTemplate): String {
    return classifyAvailabilityNotice(item.projectedLoad())
}
