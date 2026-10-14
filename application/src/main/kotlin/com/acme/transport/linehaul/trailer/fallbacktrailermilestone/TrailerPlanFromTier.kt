package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.linehaul.relay.tier.StrictRelayTier
import com.acme.transport.linehaul.relay.tier.normalizeStrictRelayTierCode

fun summarizeStrictRelayTiersForPlan(items: MutableList<StrictRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveTrailerPlansFromTier(items: MutableList<StrictRelayTier>): MutableList<TrailerPlan> {
    val out = mutableListOf<TrailerPlan>()
    for (item in items) {
        val derived = newTrailerPlan(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankTrailerPlan(item: TrailerPlan): String = normalizeStrictRelayTierCode(item.code)
