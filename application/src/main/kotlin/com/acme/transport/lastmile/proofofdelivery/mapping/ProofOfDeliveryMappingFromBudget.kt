package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.lastmile.drop.exception.SeasonalDropBudget
import com.acme.transport.lastmile.drop.exception.seasonalDropBudgetWeight

fun summarizeSeasonalDropBudgetsForMapping(items: MutableList<SeasonalDropBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveProofOfDeliveryMappingsFromBudget(items: MutableList<SeasonalDropBudget>): MutableList<ProofOfDeliveryMapping> {
    val out = mutableListOf<ProofOfDeliveryMapping>()
    for (item in items) {
        val derived = newProofOfDeliveryMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitProofOfDeliveryMapping(item: ProofOfDeliveryMapping): Double = seasonalDropBudgetWeight(4, true)
