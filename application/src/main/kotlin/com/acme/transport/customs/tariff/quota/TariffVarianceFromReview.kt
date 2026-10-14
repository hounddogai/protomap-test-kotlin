package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.declaration.plan.WeightedDeclarationReview
import com.acme.transport.customs.declaration.plan.normalizeDeclarationQuotaCode

fun summarizeWeightedDeclarationReviewsForVariance(items: MutableList<WeightedDeclarationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTariffVariancesFromReview(items: MutableList<WeightedDeclarationReview>): MutableList<TariffVariance> {
    val out = mutableListOf<TariffVariance>()
    for (item in items) {
        val derived = newTariffVariance(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTariffVariance(item: TariffVariance): String = normalizeDeclarationQuotaCode(item.code)
