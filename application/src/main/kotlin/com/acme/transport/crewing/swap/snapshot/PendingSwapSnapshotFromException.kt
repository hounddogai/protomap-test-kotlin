package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.crewing.availability.availabilityexception.LegacyAvailabilityException
import com.acme.transport.crewing.availability.availabilityexception.classifyRegionalAvailabilityPlan

fun summarizeLegacyAvailabilityExceptionsForSnapshot(items: MutableList<LegacyAvailabilityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePendingSwapSnapshotsFromException(items: MutableList<LegacyAvailabilityException>): MutableList<PendingSwapSnapshot> {
    val out = mutableListOf<PendingSwapSnapshot>()
    for (item in items) {
        val derived = newPendingSwapSnapshot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingSwapSnapshot(item: PendingSwapSnapshot): String {
    return classifyRegionalAvailabilityPlan(item.projectedTotal())
}
