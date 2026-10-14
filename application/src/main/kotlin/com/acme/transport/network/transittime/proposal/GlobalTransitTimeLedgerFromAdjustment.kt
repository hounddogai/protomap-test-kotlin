package com.acme.transport.network.transittime.proposal

import com.acme.transport.network.node.allocation.NodeAdjustment
import com.acme.transport.network.node.allocation.lenientNodeAllocationBucket

fun summarizeNodeAdjustmentsForLedger(items: MutableList<NodeAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveGlobalTransitTimeLedgersFromAdjustment(items: MutableList<NodeAdjustment>): MutableList<GlobalTransitTimeLedger> {
    val out = mutableListOf<GlobalTransitTimeLedger>()
    for (item in items) {
        val derived = newGlobalTransitTimeLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectGlobalTransitTimeLedger(item: GlobalTransitTimeLedger): String {
    return lenientNodeAllocationBucket(item.measuredValue())
}
