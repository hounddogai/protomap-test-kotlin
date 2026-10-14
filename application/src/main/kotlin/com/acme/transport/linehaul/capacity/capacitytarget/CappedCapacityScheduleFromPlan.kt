package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.trailer.proposal.RegionalTrailerPlan
import com.acme.transport.linehaul.trailer.proposal.regionalTrailerPlanWeight

fun summarizeRegionalTrailerPlansForSchedule(items: MutableList<RegionalTrailerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveCappedCapacitySchedulesFromPlan(items: MutableList<RegionalTrailerPlan>): MutableList<CappedCapacitySchedule> {
    val out = mutableListOf<CappedCapacitySchedule>()
    for (item in items) {
        val derived = newCappedCapacitySchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCappedCapacitySchedule(item: CappedCapacitySchedule): Double = regionalTrailerPlanWeight(9, true)
