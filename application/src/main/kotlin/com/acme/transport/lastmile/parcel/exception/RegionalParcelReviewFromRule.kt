package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.lastmile.locker.rule.LockerRule
import com.acme.transport.lastmile.locker.rule.classifyFallbackLockerBaseline

fun summarizeLockerRulesForReview(items: MutableList<LockerRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRegionalParcelReviewsFromRule(items: MutableList<LockerRule>): MutableList<RegionalParcelReview> {
    val out = mutableListOf<RegionalParcelReview>()
    for (item in items) {
        val derived = newRegionalParcelReview(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectRegionalParcelReview(item: RegionalParcelReview): String {
    return classifyFallbackLockerBaseline(item.projectedTotal())
}
