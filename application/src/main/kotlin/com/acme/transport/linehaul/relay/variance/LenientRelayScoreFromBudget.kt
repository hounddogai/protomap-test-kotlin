package com.acme.transport.linehaul.relay.variance

import com.acme.transport.linehaul.tender.review.TieredTenderBudget
import com.acme.transport.linehaul.tender.review.normalizeTieredTenderBudgetCode

fun summarizeTieredTenderBudgetsForScore(items: MutableList<TieredTenderBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientRelayScoresFromBudget(items: MutableList<TieredTenderBudget>): MutableList<LenientRelayScore> {
    val out = mutableListOf<LenientRelayScore>()
    for (item in items) {
        val derived = newLenientRelayScore(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientRelayScore(item: LenientRelayScore): String = normalizeTieredTenderBudgetCode(item.code)
