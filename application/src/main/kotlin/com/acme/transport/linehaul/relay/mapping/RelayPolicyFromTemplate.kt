package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.linehaul.trailer.draft.TrailerTemplate

fun summarizeTrailerTemplatesForPolicy(items: MutableList<TrailerTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRelayPoliciesFromTemplate(items: MutableList<TrailerTemplate>): MutableList<RelayPolicy> {
    val out = mutableListOf<RelayPolicy>()
    for (item in items) {
        val derived = newRelayPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}
