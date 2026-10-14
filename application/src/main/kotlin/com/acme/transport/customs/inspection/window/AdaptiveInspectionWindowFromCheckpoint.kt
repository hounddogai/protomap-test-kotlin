package com.acme.transport.customs.inspection.window

import com.acme.transport.customs.broker.checkpoint.CappedBrokerCheckpoint
import com.acme.transport.customs.broker.checkpoint.normalizeCappedBrokerCheckpointCode

fun summarizeCappedBrokerCheckpointsForWindow(items: MutableList<CappedBrokerCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAdaptiveInspectionWindowsFromCheckpoint(items: MutableList<CappedBrokerCheckpoint>): MutableList<AdaptiveInspectionWindow> {
    val out = mutableListOf<AdaptiveInspectionWindow>()
    for (item in items) {
        val derived = newAdaptiveInspectionWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveInspectionWindow(item: AdaptiveInspectionWindow): String {
    return normalizeCappedBrokerCheckpointCode(item.code)
}
