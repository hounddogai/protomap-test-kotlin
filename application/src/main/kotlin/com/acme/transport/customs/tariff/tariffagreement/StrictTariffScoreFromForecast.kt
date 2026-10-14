package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.customs.declaration.window.PendingDeclarationForecast
import com.acme.transport.customs.declaration.window.classifyAdaptiveDeclarationWindow

fun summarizePendingDeclarationForecastsForScore(items: MutableList<PendingDeclarationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveStrictTariffScoresFromForecast(items: MutableList<PendingDeclarationForecast>): MutableList<StrictTariffScore> {
    val out = mutableListOf<StrictTariffScore>()
    for (item in items) {
        val derived = newStrictTariffScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeStrictTariffScore(item: StrictTariffScore): String {
    return classifyAdaptiveDeclarationWindow(item.measuredValue())
}
