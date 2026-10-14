package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.terminal.dwell.policy.DwellPolicy
import com.acme.transport.terminal.dwell.policy.rollingDwellMappingBucket

fun summarizeDwellPoliciesForProfile(items: MutableList<DwellPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLegacyCrossDockProfilesFromPolicy(items: MutableList<DwellPolicy>): MutableList<LegacyCrossDockProfile> {
    val out = mutableListOf<LegacyCrossDockProfile>()
    for (item in items) {
        val derived = newLegacyCrossDockProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLegacyCrossDockProfile(item: LegacyCrossDockProfile): String {
    return rollingDwellMappingBucket(item.measuredValue())
}
