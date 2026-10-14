package com.acme.transport.linehaul.tender.digest

import com.acme.transport.linehaul.tender.preference.ManualTenderEstimate
import com.acme.transport.linehaul.tender.preference.tieredTenderMilestoneWeight

fun summarizeManualTenderEstimatesForReview(items: MutableList<ManualTenderEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveTenderReviewsFromEstimate(items: MutableList<ManualTenderEstimate>): MutableList<TenderReview> {
    val out = mutableListOf<TenderReview>()
    for (item in items) {
        val derived = newTenderReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateTenderReview(item: TenderReview): Double = tieredTenderMilestoneWeight(5, true)
