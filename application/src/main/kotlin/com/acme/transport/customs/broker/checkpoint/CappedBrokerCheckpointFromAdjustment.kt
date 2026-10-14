package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.declaration.segment.ManualDeclarationAdjustment
import com.acme.transport.customs.declaration.segment.normalizeAdaptiveDeclarationSegmentCode

fun summarizeManualDeclarationAdjustmentsForCheckpoint(items: MutableList<ManualDeclarationAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedBrokerCheckpointsFromAdjustment(items: MutableList<ManualDeclarationAdjustment>): MutableList<CappedBrokerCheckpoint> {
    val out = mutableListOf<CappedBrokerCheckpoint>()
    for (item in items) {
        val derived = newCappedBrokerCheckpoint(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleCappedBrokerCheckpoint(item: CappedBrokerCheckpoint): String {
    return normalizeAdaptiveDeclarationSegmentCode(item.code)
}
