package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.availability.availabilitytier.PendingAvailabilityTier
import com.acme.transport.crewing.availability.availabilitytier.classifyPendingAvailabilityTier

fun summarizePendingAvailabilityTiersForRollup(items: MutableList<PendingAvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredRestBreakRollupsFromTier(items: MutableList<PendingAvailabilityTier>): MutableList<TieredRestBreakRollup> {
    val out = mutableListOf<TieredRestBreakRollup>()
    for (item in items) {
        val derived = newTieredRestBreakRollup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectTieredRestBreakRollup(item: TieredRestBreakRollup): String {
    return classifyPendingAvailabilityTier(item.measuredValue())
}
