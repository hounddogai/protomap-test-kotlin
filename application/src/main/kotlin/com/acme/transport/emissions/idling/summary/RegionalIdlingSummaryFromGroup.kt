package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.evcharge.batchedchargeslot.PrimaryEvChargeGroup
import com.acme.transport.emissions.evcharge.batchedchargeslot.primaryEvChargeGroupBucket

fun summarizePrimaryEvChargeGroupsForSummary(items: MutableList<PrimaryEvChargeGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRegionalIdlingSummariesFromGroup(items: MutableList<PrimaryEvChargeGroup>): MutableList<RegionalIdlingSummary> {
    val out = mutableListOf<RegionalIdlingSummary>()
    for (item in items) {
        val derived = newRegionalIdlingSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankRegionalIdlingSummary(item: RegionalIdlingSummary): String {
    return primaryEvChargeGroupBucket(item.weightedVolume())
}
