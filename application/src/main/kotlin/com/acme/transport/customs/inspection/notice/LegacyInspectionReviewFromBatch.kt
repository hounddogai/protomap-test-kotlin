package com.acme.transport.customs.inspection.notice

import com.acme.transport.customs.tariff.tariffbatch.CappedTariffBatch
import com.acme.transport.customs.tariff.tariffbatch.batchedTariffVarianceWeight

fun summarizeCappedTariffBatchesForReview(items: MutableList<CappedTariffBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveLegacyInspectionReviewsFromBatch(items: MutableList<CappedTariffBatch>): MutableList<LegacyInspectionReview> {
    val out = mutableListOf<LegacyInspectionReview>()
    for (item in items) {
        val derived = newLegacyInspectionReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankLegacyInspectionReview(item: LegacyInspectionReview): Double = batchedTariffVarianceWeight(6, true)
