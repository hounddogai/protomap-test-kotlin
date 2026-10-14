package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.declaration.declarationwindow.TieredDeclarationWindow
import com.acme.transport.customs.declaration.declarationwindow.lenientDeclarationAllocationBucket

fun summarizeTieredDeclarationWindowsForAudit(items: MutableList<TieredDeclarationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRegionalClearanceAuditsFromWindow(items: MutableList<TieredDeclarationWindow>): MutableList<RegionalClearanceAudit> {
    val out = mutableListOf<RegionalClearanceAudit>()
    for (item in items) {
        val derived = newRegionalClearanceAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalClearanceAudit(item: RegionalClearanceAudit): String {
    return lenientDeclarationAllocationBucket(item.effectiveTotal())
}
