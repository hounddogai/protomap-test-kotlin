package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.terminal.dockdoor.recorditem.AdaptiveDockDoorCycle
import com.acme.transport.terminal.dockdoor.recorditem.classifyDockDoorRecord

fun summarizeAdaptiveDockDoorCyclesForTemplate(items: MutableList<AdaptiveDockDoorCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun derivePrimarySortationTemplatesFromCycle(items: MutableList<AdaptiveDockDoorCycle>): MutableList<PrimarySortationTemplate> {
    val out = mutableListOf<PrimarySortationTemplate>()
    for (item in items) {
        val derived = newPrimarySortationTemplate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planPrimarySortationTemplate(item: PrimarySortationTemplate): String {
    return classifyDockDoorRecord(item.projectedValue())
}
