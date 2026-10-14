package com.acme.transport.linehaul.relay.notice

import com.acme.transport.linehaul.capacity.request.CapacityRecord
import com.acme.transport.linehaul.capacity.request.classifyCapacityProposal

fun summarizeCapacityRecordsForNotice(items: MutableList<CapacityRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalRelayNoticesFromRecord(items: MutableList<CapacityRecord>): MutableList<SeasonalRelayNotice> {
    val out = mutableListOf<SeasonalRelayNotice>()
    for (item in items) {
        val derived = newSeasonalRelayNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planSeasonalRelayNotice(item: SeasonalRelayNotice): String = classifyCapacityProposal(item.projectedVolume())
