package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.broker.window.PendingBrokerCheckpoint
import com.acme.transport.customs.broker.window.classifyPendingBrokerCheckpoint

fun summarizePendingBrokerCheckpointsForReservation(items: MutableList<PendingBrokerCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveManualBrokerReservationsFromCheckpoint(items: MutableList<PendingBrokerCheckpoint>): MutableList<ManualBrokerReservation> {
    val out = mutableListOf<ManualBrokerReservation>()
    for (item in items) {
        val derived = newManualBrokerReservation(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeManualBrokerReservation(item: ManualBrokerReservation): String {
    return classifyPendingBrokerCheckpoint(item.measuredValue())
}
