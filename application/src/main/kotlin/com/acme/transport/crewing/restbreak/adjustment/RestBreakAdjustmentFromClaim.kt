package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.availability.claim.AvailabilityClaim
import com.acme.transport.crewing.availability.claim.classifyAvailabilitySnapshot

fun summarizeAvailabilityClaimsForAdjustment(items: MutableList<AvailabilityClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRestBreakAdjustmentsFromClaim(items: MutableList<AvailabilityClaim>): MutableList<RestBreakAdjustment> {
    val out = mutableListOf<RestBreakAdjustment>()
    for (item in items) {
        val derived = newRestBreakAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRestBreakAdjustment(item: RestBreakAdjustment): String {
    return classifyAvailabilitySnapshot(item.projectedExposure())
}
