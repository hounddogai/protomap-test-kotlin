package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.declaration.summary.DeclarationWindow
import com.acme.transport.customs.declaration.summary.declarationTierBucket

fun summarizeDeclarationWindowsForManifest(items: MutableList<DeclarationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveManualClearanceManifestsFromWindow(items: MutableList<DeclarationWindow>): MutableList<ManualClearanceManifest> {
    val out = mutableListOf<ManualClearanceManifest>()
    for (item in items) {
        val derived = newManualClearanceManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectManualClearanceManifest(item: ManualClearanceManifest): String {
    return declarationTierBucket(item.netExposure())
}
