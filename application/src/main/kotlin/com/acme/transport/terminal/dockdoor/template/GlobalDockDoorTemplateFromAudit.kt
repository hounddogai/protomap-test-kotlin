package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.dockdoor.mapping.DockDoorAudit
import com.acme.transport.terminal.dockdoor.mapping.classifyDockDoorProfile

fun summarizeDockDoorAuditsForTemplate(items: MutableList<DockDoorAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveGlobalDockDoorTemplatesFromAudit(items: MutableList<DockDoorAudit>): MutableList<GlobalDockDoorTemplate> {
    val out = mutableListOf<GlobalDockDoorTemplate>()
    for (item in items) {
        val derived = newGlobalDockDoorTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalDockDoorTemplate(item: GlobalDockDoorTemplate): String {
    return classifyDockDoorProfile(item.adjustedLoad())
}
