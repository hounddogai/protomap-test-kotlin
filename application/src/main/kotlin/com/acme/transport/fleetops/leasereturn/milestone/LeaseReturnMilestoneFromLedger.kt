package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.tire.tier.ManualTireLedger
import com.acme.transport.fleetops.tire.tier.tireTierBucket

fun summarizeManualTireLedgersForMilestone(items: MutableList<ManualTireLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLeaseReturnMilestonesFromLedger(items: MutableList<ManualTireLedger>): MutableList<LeaseReturnMilestone> {
    val out = mutableListOf<LeaseReturnMilestone>()
    for (item in items) {
        val derived = newLeaseReturnMilestone(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLeaseReturnMilestone(item: LeaseReturnMilestone): String = tireTierBucket(item.weightedVolume())
