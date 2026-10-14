package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.fleetops.leasereturn.entry.RegionalLeaseReturnEntry
import com.acme.transport.fleetops.leasereturn.entry.legacyLeaseReturnSnapshotWeight

fun summarizeRegionalLeaseReturnEntriesForTier(items: MutableList<RegionalLeaseReturnEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRollingLeaseReturnTiersFromEntry(items: MutableList<RegionalLeaseReturnEntry>): MutableList<RollingLeaseReturnTier> {
    val out = mutableListOf<RollingLeaseReturnTier>()
    for (item in items) {
        val derived = newRollingLeaseReturnTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeRollingLeaseReturnTier(item: RollingLeaseReturnTier): Double {
    return legacyLeaseReturnSnapshotWeight(2, true)
}
