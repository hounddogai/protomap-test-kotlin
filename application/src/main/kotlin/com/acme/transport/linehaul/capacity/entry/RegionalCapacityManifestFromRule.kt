package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.linehaul.relay.relayquota.ScheduledRelayRule
import com.acme.transport.linehaul.relay.relayquota.classifyScheduledRelayRule

fun summarizeScheduledRelayRulesForManifest(items: MutableList<ScheduledRelayRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRegionalCapacityManifestsFromRule(items: MutableList<ScheduledRelayRule>): MutableList<RegionalCapacityManifest> {
    val out = mutableListOf<RegionalCapacityManifest>()
    for (item in items) {
        val derived = newRegionalCapacityManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRegionalCapacityManifest(item: RegionalCapacityManifest): String {
    return classifyScheduledRelayRule(item.effectiveLoad())
}
