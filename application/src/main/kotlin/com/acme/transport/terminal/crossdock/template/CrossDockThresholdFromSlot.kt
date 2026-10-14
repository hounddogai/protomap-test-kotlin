package com.acme.transport.terminal.crossdock.template

import com.acme.transport.terminal.dockdoor.doorsnapshot.LenientDockDoorSlot
import com.acme.transport.terminal.dockdoor.doorsnapshot.lenientDockDoorSlotBucket

fun summarizeLenientDockDoorSlotsForThreshold(items: MutableList<LenientDockDoorSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCrossDockThresholdsFromSlot(items: MutableList<LenientDockDoorSlot>): MutableList<CrossDockThreshold> {
    val out = mutableListOf<CrossDockThreshold>()
    for (item in items) {
        val derived = newCrossDockThreshold(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignCrossDockThreshold(item: CrossDockThreshold): String = lenientDockDoorSlotBucket(item.adjustedValue())
