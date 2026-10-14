package com.acme.transport.linehaul.trailer.group

import com.acme.transport.linehaul.relay.checkpoint.RegionalRelayPolicy
import com.acme.transport.linehaul.relay.checkpoint.normalizeRelayCheckpointCode

fun summarizeRegionalRelayPoliciesForGroup(items: MutableList<RegionalRelayPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveStrictTrailerGroupsFromPolicy(items: MutableList<RegionalRelayPolicy>): MutableList<StrictTrailerGroup> {
    val out = mutableListOf<StrictTrailerGroup>()
    for (item in items) {
        val derived = newStrictTrailerGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateStrictTrailerGroup(item: StrictTrailerGroup): String = normalizeRelayCheckpointCode(item.code)
