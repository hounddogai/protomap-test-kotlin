package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.crewing.availability.availabilityscore.CappedAvailabilityRollup
import com.acme.transport.crewing.availability.availabilityscore.weightedAvailabilityGroupWeight

fun summarizeCappedAvailabilityRollupsForLedger(items: MutableList<CappedAvailabilityRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveSeasonalRestBreakLedgersFromRollup(items: MutableList<CappedAvailabilityRollup>): MutableList<SeasonalRestBreakLedger> {
    val out = mutableListOf<SeasonalRestBreakLedger>()
    for (item in items) {
        val derived = newSeasonalRestBreakLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateSeasonalRestBreakLedger(item: SeasonalRestBreakLedger): Double {
    return weightedAvailabilityGroupWeight(4, true)
}
