package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.tire.overrideitem.PrimaryTireOverride
import com.acme.transport.fleetops.tire.overrideitem.normalizeBatchedTireTierCode

fun summarizePrimaryTireOverridesForBaseline(items: MutableList<PrimaryTireOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveStrictLeaseReturnBaselinesFromOverride(items: MutableList<PrimaryTireOverride>): MutableList<StrictLeaseReturnBaseline> {
    val out = mutableListOf<StrictLeaseReturnBaseline>()
    for (item in items) {
        val derived = newStrictLeaseReturnBaseline(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeStrictLeaseReturnBaseline(item: StrictLeaseReturnBaseline): String {
    return normalizeBatchedTireTierCode(item.code)
}
