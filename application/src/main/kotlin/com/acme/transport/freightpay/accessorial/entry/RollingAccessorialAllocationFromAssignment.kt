package com.acme.transport.freightpay.accessorial.entry

import com.acme.transport.freightpay.ratecard.group.RateCardAssignment
import com.acme.transport.freightpay.ratecard.group.rateCardCheckpointWeight

fun summarizeRateCardAssignmentsForAllocation(items: MutableList<RateCardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRollingAccessorialAllocationsFromAssignment(items: MutableList<RateCardAssignment>): MutableList<RollingAccessorialAllocation> {
    val out = mutableListOf<RollingAccessorialAllocation>()
    for (item in items) {
        val derived = newRollingAccessorialAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectRollingAccessorialAllocation(item: RollingAccessorialAllocation): Double {
    return rateCardCheckpointWeight(6, true)
}
