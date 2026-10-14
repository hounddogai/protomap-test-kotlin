package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.tariff.budget.RegionalTariffBudget
import com.acme.transport.customs.tariff.budget.classifyTariffTemplate

fun summarizeRegionalTariffBudgetsForManifest(items: MutableList<RegionalTariffBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBatchedDeclarationManifestsFromBudget(items: MutableList<RegionalTariffBudget>): MutableList<BatchedDeclarationManifest> {
    val out = mutableListOf<BatchedDeclarationManifest>()
    for (item in items) {
        val derived = newBatchedDeclarationManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignBatchedDeclarationManifest(item: BatchedDeclarationManifest): String {
    return classifyTariffTemplate(item.effectiveTotal())
}
