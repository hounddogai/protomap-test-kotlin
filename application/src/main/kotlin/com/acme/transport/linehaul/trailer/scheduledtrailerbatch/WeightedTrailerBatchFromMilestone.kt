package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.linehaul.relay.audit.SeasonalRelayMilestone
import com.acme.transport.linehaul.relay.audit.relayAuditBucket

fun summarizeSeasonalRelayMilestonesForBatch(items: MutableList<SeasonalRelayMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedTrailerBatchesFromMilestone(items: MutableList<SeasonalRelayMilestone>): MutableList<WeightedTrailerBatch> {
    val out = mutableListOf<WeightedTrailerBatch>()
    for (item in items) {
        val derived = newWeightedTrailerBatch(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun applyWeightedTrailerBatch(item: WeightedTrailerBatch): String = relayAuditBucket(item.netLoad())
