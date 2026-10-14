package com.acme.transport.crewing.roster.request

import com.acme.transport.crewing.availability.availabilityledger.TieredAvailabilityLedger
import com.acme.transport.crewing.availability.availabilityledger.classifyCappedAvailabilityPreference

fun summarizeTieredAvailabilityLedgersForAdjustment(items: MutableList<TieredAvailabilityLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveGlobalRosterAdjustmentsFromLedger(items: MutableList<TieredAvailabilityLedger>): MutableList<GlobalRosterAdjustment> {
    val out = mutableListOf<GlobalRosterAdjustment>()
    for (item in items) {
        val derived = newGlobalRosterAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleGlobalRosterAdjustment(item: GlobalRosterAdjustment): String {
    return classifyCappedAvailabilityPreference(item.projectedValue())
}
