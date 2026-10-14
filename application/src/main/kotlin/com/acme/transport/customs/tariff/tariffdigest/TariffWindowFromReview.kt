package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.declaration.schedule.FallbackDeclarationReview
import com.acme.transport.customs.declaration.schedule.seasonalDeclarationScheduleBucket

fun summarizeFallbackDeclarationReviewsForWindow(items: MutableList<FallbackDeclarationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTariffWindowsFromReview(items: MutableList<FallbackDeclarationReview>): MutableList<TariffWindow> {
    val out = mutableListOf<TariffWindow>()
    for (item in items) {
        val derived = newTariffWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyTariffWindow(item: TariffWindow): String = seasonalDeclarationScheduleBucket(item.measuredValue())
