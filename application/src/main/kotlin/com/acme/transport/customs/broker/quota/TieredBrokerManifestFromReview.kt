package com.acme.transport.customs.broker.quota

import com.acme.transport.customs.declaration.schedule.FallbackDeclarationReview
import com.acme.transport.customs.declaration.schedule.seasonalDeclarationScheduleBucket

fun summarizeFallbackDeclarationReviewsForManifest(items: MutableList<FallbackDeclarationReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTieredBrokerManifestsFromReview(items: MutableList<FallbackDeclarationReview>): MutableList<TieredBrokerManifest> {
    val out = mutableListOf<TieredBrokerManifest>()
    for (item in items) {
        val derived = newTieredBrokerManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTieredBrokerManifest(item: TieredBrokerManifest): String {
    return seasonalDeclarationScheduleBucket(item.netValue())
}
