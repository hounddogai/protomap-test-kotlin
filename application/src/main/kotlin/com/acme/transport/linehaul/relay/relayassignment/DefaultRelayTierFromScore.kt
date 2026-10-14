package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.linehaul.backhaul.cappedbackhaulvariance.PendingBackhaulScore
import com.acme.transport.linehaul.backhaul.cappedbackhaulvariance.classifyLenientBackhaulVariance

fun summarizePendingBackhaulScoresForTier(items: MutableList<PendingBackhaulScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultRelayTiersFromScore(items: MutableList<PendingBackhaulScore>): MutableList<DefaultRelayTier> {
    val out = mutableListOf<DefaultRelayTier>()
    for (item in items) {
        val derived = newDefaultRelayTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planDefaultRelayTier(item: DefaultRelayTier): String = classifyLenientBackhaulVariance(item.weightedValue())
