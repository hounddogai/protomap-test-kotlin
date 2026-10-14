package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.crewing.availability.weightedavailabilityreservation.WeightedAvailabilityDraft
import com.acme.transport.crewing.availability.weightedavailabilityreservation.classifyAvailabilityProposal

fun summarizeWeightedAvailabilityDraftsForSegment(items: MutableList<WeightedAvailabilityDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveScheduledAvailabilitySegmentsFromDraft(items: MutableList<WeightedAvailabilityDraft>): MutableList<ScheduledAvailabilitySegment> {
    val out = mutableListOf<ScheduledAvailabilitySegment>()
    for (item in items) {
        val derived = newScheduledAvailabilitySegment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankScheduledAvailabilitySegment(item: ScheduledAvailabilitySegment): String {
    return classifyAvailabilityProposal(item.weightedTotal())
}
