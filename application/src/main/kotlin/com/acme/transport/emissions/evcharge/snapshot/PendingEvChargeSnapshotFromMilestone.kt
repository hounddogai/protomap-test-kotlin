package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.fuelburn.burnmilestone.RollingFuelBurnMilestone
import com.acme.transport.emissions.fuelburn.burnmilestone.rollingFuelBurnMilestoneBucket

fun summarizeRollingFuelBurnMilestonesForSnapshot(items: MutableList<RollingFuelBurnMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun derivePendingEvChargeSnapshotsFromMilestone(items: MutableList<RollingFuelBurnMilestone>): MutableList<PendingEvChargeSnapshot> {
    val out = mutableListOf<PendingEvChargeSnapshot>()
    for (item in items) {
        val derived = newPendingEvChargeSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcilePendingEvChargeSnapshot(item: PendingEvChargeSnapshot): String {
    return rollingFuelBurnMilestoneBucket(item.measuredValue())
}
