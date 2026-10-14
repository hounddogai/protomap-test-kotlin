package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.availability.defaultavailabilitysegment.StrictAvailabilityException
import com.acme.transport.crewing.availability.defaultavailabilitysegment.classifyScheduledAvailabilitySegment

fun summarizeStrictAvailabilityExceptionsForClaim(items: MutableList<StrictAvailabilityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictRestBreakClaimsFromException(items: MutableList<StrictAvailabilityException>): MutableList<StrictRestBreakClaim> {
    val out = mutableListOf<StrictRestBreakClaim>()
    for (item in items) {
        val derived = newStrictRestBreakClaim(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchStrictRestBreakClaim(item: StrictRestBreakClaim): String {
    return classifyScheduledAvailabilitySegment(item.adjustedValue())
}
