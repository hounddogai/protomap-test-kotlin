package com.acme.transport.lastmile.drop.profile

import com.acme.transport.lastmile.proofofdelivery.budget.BatchedProofOfDeliveryBudget
import com.acme.transport.lastmile.proofofdelivery.budget.classifyProofOfDeliveryBaseline

fun summarizeBatchedProofOfDeliveryBudgetsForRecord(items: MutableList<BatchedProofOfDeliveryBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAdaptiveDropRecordsFromBudget(items: MutableList<BatchedProofOfDeliveryBudget>): MutableList<AdaptiveDropRecord> {
    val out = mutableListOf<AdaptiveDropRecord>()
    for (item in items) {
        val derived = newAdaptiveDropRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planAdaptiveDropRecord(item: AdaptiveDropRecord): String = classifyProofOfDeliveryBaseline(item.netVolume())
