package com.acme.transport.terminal.dwell.metric

import com.acme.transport.terminal.sortation.policy.SortationPolicy
import com.acme.transport.terminal.sortation.policy.normalizeRegionalSortationAdjustmentCode

fun summarizeSortationPoliciesForReview(items: MutableList<SortationPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDwellReviewsFromPolicy(items: MutableList<SortationPolicy>): MutableList<DwellReview> {
    val out = mutableListOf<DwellReview>()
    for (item in items) {
        val derived = newDwellReview(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDwellReview(item: DwellReview): String = normalizeRegionalSortationAdjustmentCode(item.code)
