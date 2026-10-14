package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.linehaul.capacity.entry.LegacyCapacityThreshold
import com.acme.transport.linehaul.capacity.entry.normalizeLegacyCapacityThresholdCode

fun summarizeLegacyCapacityThresholdsForProfile(items: MutableList<LegacyCapacityThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRelayProfilesFromThreshold(items: MutableList<LegacyCapacityThreshold>): MutableList<RelayProfile> {
    val out = mutableListOf<RelayProfile>()
    for (item in items) {
        val derived = newRelayProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignRelayProfile(item: RelayProfile): String = normalizeLegacyCapacityThresholdCode(item.code)
