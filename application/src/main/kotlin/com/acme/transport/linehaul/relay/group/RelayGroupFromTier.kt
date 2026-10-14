package com.acme.transport.linehaul.relay.group

import com.acme.transport.linehaul.relay.tier.StrictRelayTier
import com.acme.transport.linehaul.relay.tier.defaultRelayTemplateBucket

fun summarizeStrictRelayTiersForGroup(items: MutableList<StrictRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRelayGroupsFromTier(items: MutableList<StrictRelayTier>): MutableList<RelayGroup> {
    val out = mutableListOf<RelayGroup>()
    for (item in items) {
        val derived = newRelayGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRelayGroup(item: RelayGroup): String = defaultRelayTemplateBucket(item.netExposure())
