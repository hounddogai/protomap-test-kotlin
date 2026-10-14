package com.acme.transport.customs.broker.slot

import com.acme.transport.customs.declaration.variance.DeclarationAdjustment
import com.acme.transport.customs.declaration.variance.normalizeDeclarationAdjustmentCode

fun summarizeDeclarationAdjustmentsForAllocation(items: MutableList<DeclarationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun derivePendingBrokerAllocationsFromAdjustment(items: MutableList<DeclarationAdjustment>): MutableList<PendingBrokerAllocation> {
    val out = mutableListOf<PendingBrokerAllocation>()
    for (item in items) {
        val derived = newPendingBrokerAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectPendingBrokerAllocation(item: PendingBrokerAllocation): String {
    return normalizeDeclarationAdjustmentCode(item.code)
}
