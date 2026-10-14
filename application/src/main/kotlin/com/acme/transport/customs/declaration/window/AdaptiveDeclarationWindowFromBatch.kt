package com.acme.transport.customs.declaration.window

import com.acme.transport.customs.tariff.tariffbatch.CappedTariffBatch
import com.acme.transport.customs.tariff.tariffbatch.classifyCappedTariffBatch

fun summarizeCappedTariffBatchesForWindow(items: MutableList<CappedTariffBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAdaptiveDeclarationWindowsFromBatch(items: MutableList<CappedTariffBatch>): MutableList<AdaptiveDeclarationWindow> {
    val out = mutableListOf<AdaptiveDeclarationWindow>()
    for (item in items) {
        val derived = newAdaptiveDeclarationWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleAdaptiveDeclarationWindow(item: AdaptiveDeclarationWindow): String {
    return classifyCappedTariffBatch(item.adjustedValue())
}
