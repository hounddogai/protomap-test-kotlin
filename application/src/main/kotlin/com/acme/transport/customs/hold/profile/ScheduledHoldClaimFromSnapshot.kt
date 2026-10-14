package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.broker.brokerslot.PrimaryBrokerSnapshot
import com.acme.transport.customs.broker.brokerslot.rollingBrokerSlotBucket

fun summarizePrimaryBrokerSnapshotsForClaim(items: MutableList<PrimaryBrokerSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveScheduledHoldClaimsFromSnapshot(items: MutableList<PrimaryBrokerSnapshot>): MutableList<ScheduledHoldClaim> {
    val out = mutableListOf<ScheduledHoldClaim>()
    for (item in items) {
        val derived = newScheduledHoldClaim(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planScheduledHoldClaim(item: ScheduledHoldClaim): String = rollingBrokerSlotBucket(item.projectedValue())
