package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.hold.holdaudit.PrimaryHoldLedger
import com.acme.transport.customs.hold.holdaudit.classifyPrimaryHoldLedger

fun summarizePrimaryHoldLedgersForCheckpoint(items: MutableList<PrimaryHoldLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientClearanceCheckpointsFromLedger(items: MutableList<PrimaryHoldLedger>): MutableList<LenientClearanceCheckpoint> {
    val out = mutableListOf<LenientClearanceCheckpoint>()
    for (item in items) {
        val derived = newLenientClearanceCheckpoint(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectLenientClearanceCheckpoint(item: LenientClearanceCheckpoint): String {
    return classifyPrimaryHoldLedger(item.adjustedValue())
}
