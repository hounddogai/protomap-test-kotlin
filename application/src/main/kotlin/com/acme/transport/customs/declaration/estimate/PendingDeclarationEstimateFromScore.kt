package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.tariff.tariffscore.AdaptiveTariffScore
import com.acme.transport.customs.tariff.tariffscore.classifyDefaultTariffTarget

fun summarizeAdaptiveTariffScoresForEstimate(items: MutableList<AdaptiveTariffScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun derivePendingDeclarationEstimatesFromScore(items: MutableList<AdaptiveTariffScore>): MutableList<PendingDeclarationEstimate> {
    val out = mutableListOf<PendingDeclarationEstimate>()
    for (item in items) {
        val derived = newPendingDeclarationEstimate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchPendingDeclarationEstimate(item: PendingDeclarationEstimate): String {
    return classifyDefaultTariffTarget(item.netExposure())
}
