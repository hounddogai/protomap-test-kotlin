package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.backhaul.backhaulaudit.BackhaulPlan
import com.acme.transport.linehaul.backhaul.backhaulaudit.cappedBackhaulLedgerWeight

fun summarizeBackhaulPlansForReview(items: MutableList<BackhaulPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFallbackLanePlanReviewsFromPlan(items: MutableList<BackhaulPlan>): MutableList<FallbackLanePlanReview> {
    val out = mutableListOf<FallbackLanePlanReview>()
    for (item in items) {
        val derived = newFallbackLanePlanReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFallbackLanePlanReview(item: FallbackLanePlanReview): Double = cappedBackhaulLedgerWeight(7, true)
