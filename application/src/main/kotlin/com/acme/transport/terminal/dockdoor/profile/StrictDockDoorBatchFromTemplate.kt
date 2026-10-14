package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.terminal.dockdoor.rollingdoorsnapshot.PrimaryDockDoorTemplate
import com.acme.transport.terminal.dockdoor.rollingdoorsnapshot.classifySeasonalDockDoorSnapshot

fun summarizePrimaryDockDoorTemplatesForBatch(items: MutableList<PrimaryDockDoorTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveStrictDockDoorBatchesFromTemplate(items: MutableList<PrimaryDockDoorTemplate>): MutableList<StrictDockDoorBatch> {
    val out = mutableListOf<StrictDockDoorBatch>()
    for (item in items) {
        val derived = newStrictDockDoorBatch(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileStrictDockDoorBatch(item: StrictDockDoorBatch): String {
    return classifySeasonalDockDoorSnapshot(item.measuredValue())
}
