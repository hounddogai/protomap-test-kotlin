package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.linehaul.backhaul.backhaulassignment.BatchedBackhaulAssignment
import com.acme.transport.linehaul.backhaul.backhaulassignment.classifyPendingBackhaulMapping

fun summarizeBatchedBackhaulAssignmentsForThreshold(items: MutableList<BatchedBackhaulAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingRelayThresholdsFromAssignment(items: MutableList<BatchedBackhaulAssignment>): MutableList<PendingRelayThreshold> {
    val out = mutableListOf<PendingRelayThreshold>()
    for (item in items) {
        val derived = newPendingRelayThreshold(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankPendingRelayThreshold(item: PendingRelayThreshold): String {
    return classifyPendingBackhaulMapping(item.weightedTotal())
}
