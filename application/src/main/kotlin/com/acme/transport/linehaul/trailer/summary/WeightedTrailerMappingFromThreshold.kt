package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.linehaul.relay.relaypolicy.PendingRelayThreshold
import com.acme.transport.linehaul.relay.relaypolicy.classifyPendingRelayThreshold

fun summarizePendingRelayThresholdsForMapping(items: MutableList<PendingRelayThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveWeightedTrailerMappingsFromThreshold(items: MutableList<PendingRelayThreshold>): MutableList<WeightedTrailerMapping> {
    val out = mutableListOf<WeightedTrailerMapping>()
    for (item in items) {
        val derived = newWeightedTrailerMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planWeightedTrailerMapping(item: WeightedTrailerMapping): String {
    return classifyPendingRelayThreshold(item.weightedValue())
}
