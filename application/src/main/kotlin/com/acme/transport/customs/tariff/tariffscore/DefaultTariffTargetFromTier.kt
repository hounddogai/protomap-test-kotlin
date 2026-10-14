package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.customs.declaration.budget.RollingDeclarationTier
import com.acme.transport.customs.declaration.budget.strictDeclarationBudgetBucket

fun summarizeRollingDeclarationTiersForTarget(items: MutableList<RollingDeclarationTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDefaultTariffTargetsFromTier(items: MutableList<RollingDeclarationTier>): MutableList<DefaultTariffTarget> {
    val out = mutableListOf<DefaultTariffTarget>()
    for (item in items) {
        val derived = newDefaultTariffTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreDefaultTariffTarget(item: DefaultTariffTarget): String {
    return strictDeclarationBudgetBucket(item.adjustedVolume())
}
