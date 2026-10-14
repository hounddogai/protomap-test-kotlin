package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.crewing.availability.seasonalavailabilitydigest.WeightedAvailabilitySlot
import com.acme.transport.crewing.availability.seasonalavailabilitydigest.classifyWeightedAvailabilitySlot

fun summarizeWeightedAvailabilitySlotsForSummary(items: MutableList<WeightedAvailabilitySlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultQualificationSummariesFromSlot(items: MutableList<WeightedAvailabilitySlot>): MutableList<DefaultQualificationSummary> {
    val out = mutableListOf<DefaultQualificationSummary>()
    for (item in items) {
        val derived = newDefaultQualificationSummary(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitDefaultQualificationSummary(item: DefaultQualificationSummary): String {
    return classifyWeightedAvailabilitySlot(item.measuredValue())
}
