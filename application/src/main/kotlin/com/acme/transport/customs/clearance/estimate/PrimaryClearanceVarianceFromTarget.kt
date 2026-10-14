package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.declaration.variance.SeasonalDeclarationTarget
import com.acme.transport.customs.declaration.variance.normalizePendingDeclarationVarianceCode

fun summarizeSeasonalDeclarationTargetsForVariance(items: MutableList<SeasonalDeclarationTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun derivePrimaryClearanceVariancesFromTarget(items: MutableList<SeasonalDeclarationTarget>): MutableList<PrimaryClearanceVariance> {
    val out = mutableListOf<PrimaryClearanceVariance>()
    for (item in items) {
        val derived = newPrimaryClearanceVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryClearanceVariance(item: PrimaryClearanceVariance): String {
    return normalizePendingDeclarationVarianceCode(item.code)
}
