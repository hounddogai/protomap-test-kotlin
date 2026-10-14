package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.linehaul.relay.manifest.RelayManifest
import com.acme.transport.linehaul.relay.manifest.normalizeRelayDraftCode

fun summarizeRelayManifestsForPreference(items: MutableList<RelayManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCapacityPreferencesFromManifest(items: MutableList<RelayManifest>): MutableList<CapacityPreference> {
    val out = mutableListOf<CapacityPreference>()
    for (item in items) {
        val derived = newCapacityPreference(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectCapacityPreference(item: CapacityPreference): String = normalizeRelayDraftCode(item.code)
