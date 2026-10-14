package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.linehaul.capacity.slot.RegionalCapacityRequest
import com.acme.transport.linehaul.capacity.slot.regionalCapacityRequestWeight

fun summarizeRegionalCapacityRequestsForEstimate(items: MutableList<RegionalCapacityRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveTenderEstimatesFromRequest(items: MutableList<RegionalCapacityRequest>): MutableList<TenderEstimate> {
    val out = mutableListOf<TenderEstimate>()
    for (item in items) {
        val derived = newTenderEstimate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveTenderEstimate(item: TenderEstimate): Double = regionalCapacityRequestWeight(6, true)
