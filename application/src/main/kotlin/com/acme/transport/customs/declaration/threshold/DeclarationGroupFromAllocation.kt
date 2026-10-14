package com.acme.transport.customs.declaration.threshold

import com.acme.transport.customs.clearance.variance.DefaultClearanceAllocation
import com.acme.transport.customs.clearance.variance.normalizeClearanceWindowCode

fun summarizeDefaultClearanceAllocationsForGroup(items: MutableList<DefaultClearanceAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveDeclarationGroupsFromAllocation(items: MutableList<DefaultClearanceAllocation>): MutableList<DeclarationGroup> {
    val out = mutableListOf<DeclarationGroup>()
    for (item in items) {
        val derived = newDeclarationGroup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceDeclarationGroup(item: DeclarationGroup): String = normalizeClearanceWindowCode(item.code)
