package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.terminal.dockdoor.doorsnapshot.PendingDockDoorException
import com.acme.transport.terminal.dockdoor.doorsnapshot.classifyCappedDockDoorSnapshot

fun summarizePendingDockDoorExceptionsForPolicy(items: MutableList<PendingDockDoorException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveLenientDockDoorPoliciesFromException(items: MutableList<PendingDockDoorException>): MutableList<LenientDockDoorPolicy> {
    val out = mutableListOf<LenientDockDoorPolicy>()
    for (item in items) {
        val derived = newLenientDockDoorPolicy(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLenientDockDoorPolicy(item: LenientDockDoorPolicy): String {
    return classifyCappedDockDoorSnapshot(item.adjustedVolume())
}
