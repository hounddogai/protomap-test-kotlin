package com.acme.transport.customs.declaration.budget

import com.acme.transport.customs.declaration.window.AdaptiveDeclarationWindow
import com.acme.transport.customs.declaration.window.classifyPendingDeclarationForecast

fun summarizeAdaptiveDeclarationWindowsForBaseline(items: MutableList<AdaptiveDeclarationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredDeclarationBaselinesFromWindow(items: MutableList<AdaptiveDeclarationWindow>): MutableList<TieredDeclarationBaseline> {
    val out = mutableListOf<TieredDeclarationBaseline>()
    for (item in items) {
        val derived = newTieredDeclarationBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyTieredDeclarationBaseline(item: TieredDeclarationBaseline): String {
    return classifyPendingDeclarationForecast(item.effectiveExposure())
}
