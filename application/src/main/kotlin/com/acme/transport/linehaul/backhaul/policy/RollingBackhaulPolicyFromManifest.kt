package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.capacity.entry.RegionalCapacityManifest
import com.acme.transport.linehaul.capacity.entry.normalizeGlobalCapacityReservationCode

fun summarizeRegionalCapacityManifestsForPolicy(items: MutableList<RegionalCapacityManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingBackhaulPoliciesFromManifest(items: MutableList<RegionalCapacityManifest>): MutableList<RollingBackhaulPolicy> {
    val out = mutableListOf<RollingBackhaulPolicy>()
    for (item in items) {
        val derived = newRollingBackhaulPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRollingBackhaulPolicy(item: RollingBackhaulPolicy): String {
    return normalizeGlobalCapacityReservationCode(item.code)
}
