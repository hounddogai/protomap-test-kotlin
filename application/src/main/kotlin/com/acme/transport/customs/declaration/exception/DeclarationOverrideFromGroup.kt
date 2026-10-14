package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clearance.target.PrimaryClearanceGroup
import com.acme.transport.customs.clearance.target.classifyPrimaryClearanceGroup

fun summarizePrimaryClearanceGroupsForOverride(items: MutableList<PrimaryClearanceGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDeclarationOverridesFromGroup(items: MutableList<PrimaryClearanceGroup>): MutableList<DeclarationOverride> {
    val out = mutableListOf<DeclarationOverride>()
    for (item in items) {
        val derived = newDeclarationOverride(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDeclarationOverride(item: DeclarationOverride): String {
    return classifyPrimaryClearanceGroup(item.adjustedVolume())
}
