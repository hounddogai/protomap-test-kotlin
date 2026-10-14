package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.customs.clearance.clearancethreshold.LenientClearanceThreshold
import com.acme.transport.customs.clearance.clearancethreshold.manualClearancePlanWeight

fun summarizeLenientClearanceThresholdsForGroup(items: MutableList<LenientClearanceThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRollingDeclarationGroupsFromThreshold(items: MutableList<LenientClearanceThreshold>): MutableList<RollingDeclarationGroup> {
    val out = mutableListOf<RollingDeclarationGroup>()
    for (item in items) {
        val derived = newRollingDeclarationGroup(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRollingDeclarationGroup(item: RollingDeclarationGroup): Double = manualClearancePlanWeight(5, true)
