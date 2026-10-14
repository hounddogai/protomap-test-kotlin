package com.acme.transport.linehaul.relay.group

import com.acme.transport.linehaul.trailer.fallbacktrailermilestone.RegionalTrailerBatch
import com.acme.transport.linehaul.trailer.fallbacktrailermilestone.classifyTrailerPlan

fun summarizeRegionalTrailerBatchesForNotice(items: MutableList<RegionalTrailerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedRelayNoticesFromBatch(items: MutableList<RegionalTrailerBatch>): MutableList<CappedRelayNotice> {
    val out = mutableListOf<CappedRelayNotice>()
    for (item in items) {
        val derived = newCappedRelayNotice(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileCappedRelayNotice(item: CappedRelayNotice): String = classifyTrailerPlan(item.measuredValue())
