package com.acme.transport.customs.broker.slot

import com.acme.transport.customs.tariff.variance.DefaultTariffEstimate
import com.acme.transport.customs.tariff.variance.globalTariffSlotBucket

fun summarizeDefaultTariffEstimatesForAllocation(items: MutableList<DefaultTariffEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePendingBrokerAllocationsFromEstimate(items: MutableList<DefaultTariffEstimate>): MutableList<PendingBrokerAllocation> {
    val out = mutableListOf<PendingBrokerAllocation>()
    for (item in items) {
        val derived = newPendingBrokerAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectPendingBrokerAllocationExtra(item: PendingBrokerAllocation): String {
    return globalTariffSlotBucket(item.projectedVolume())
}
