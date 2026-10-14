package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.declaration.declarationwindow.TieredDeclarationScore
import com.acme.transport.customs.declaration.declarationwindow.tieredDeclarationWindowWeight

fun summarizeTieredDeclarationScoresForOverride(items: MutableList<TieredDeclarationScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackInspectionOverridesFromScore(items: MutableList<TieredDeclarationScore>): MutableList<FallbackInspectionOverride> {
    val out = mutableListOf<FallbackInspectionOverride>()
    for (item in items) {
        val derived = newFallbackInspectionOverride(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveFallbackInspectionOverride(item: FallbackInspectionOverride): Double {
    return tieredDeclarationWindowWeight(2, true)
}
