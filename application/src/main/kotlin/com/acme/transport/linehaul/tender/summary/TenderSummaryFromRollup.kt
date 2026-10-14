package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.capacity.summary.PendingCapacityRollup
import com.acme.transport.linehaul.capacity.summary.classifyPendingCapacityRollup

fun summarizePendingCapacityRollupsForSummary(items: MutableList<PendingCapacityRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTenderSummariesFromRollup(items: MutableList<PendingCapacityRollup>): MutableList<TenderSummary> {
    val out = mutableListOf<TenderSummary>()
    for (item in items) {
        val derived = newTenderSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignTenderSummary(item: TenderSummary): String = classifyPendingCapacityRollup(item.netVolume())
