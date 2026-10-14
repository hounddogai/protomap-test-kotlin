package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.terminal.crossdock.rollup.TieredCrossDockClaim
import com.acme.transport.terminal.crossdock.rollup.tieredCrossDockRollupWeight

fun summarizeTieredCrossDockClaimsForReview(items: MutableList<TieredCrossDockClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveLegacySortationReviewsFromClaim(items: MutableList<TieredCrossDockClaim>): MutableList<LegacySortationReview> {
    val out = mutableListOf<LegacySortationReview>()
    for (item in items) {
        val derived = newLegacySortationReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLegacySortationReview(item: LegacySortationReview): Double = tieredCrossDockRollupWeight(6, true)
