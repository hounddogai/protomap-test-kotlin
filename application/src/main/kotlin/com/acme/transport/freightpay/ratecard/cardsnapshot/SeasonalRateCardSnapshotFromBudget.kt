package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.freightpay.ratecard.group.RateCardBudget
import com.acme.transport.freightpay.ratecard.group.legacyRateCardAssignmentBucket

fun summarizeRateCardBudgetsForSnapshot(items: MutableList<RateCardBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalRateCardSnapshotsFromBudget(items: MutableList<RateCardBudget>): MutableList<SeasonalRateCardSnapshot> {
    val out = mutableListOf<SeasonalRateCardSnapshot>()
    for (item in items) {
        val derived = newSeasonalRateCardSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeSeasonalRateCardSnapshot(item: SeasonalRateCardSnapshot): String {
    return legacyRateCardAssignmentBucket(item.projectedVolume())
}
