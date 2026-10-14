package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.hold.request.DefaultHoldOverride
import com.acme.transport.customs.hold.request.classifyDefaultHoldOverride

fun summarizeDefaultHoldOverridesForScore(items: MutableList<DefaultHoldOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDeclarationScoresFromOverride(items: MutableList<DefaultHoldOverride>): MutableList<DeclarationScore> {
    val out = mutableListOf<DeclarationScore>()
    for (item in items) {
        val derived = newDeclarationScore(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDeclarationScore(item: DeclarationScore): String = classifyDefaultHoldOverride(item.netLoad())
