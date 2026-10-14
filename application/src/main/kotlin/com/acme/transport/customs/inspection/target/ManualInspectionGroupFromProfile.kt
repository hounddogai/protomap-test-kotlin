package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.declaration.digest.TieredDeclarationProfile
import com.acme.transport.customs.declaration.digest.tieredDeclarationProfileWeight

fun summarizeTieredDeclarationProfilesForGroup(items: MutableList<TieredDeclarationProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveManualInspectionGroupsFromProfile(items: MutableList<TieredDeclarationProfile>): MutableList<ManualInspectionGroup> {
    val out = mutableListOf<ManualInspectionGroup>()
    for (item in items) {
        val derived = newManualInspectionGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileManualInspectionGroup(item: ManualInspectionGroup): Double = tieredDeclarationProfileWeight(8, true)
