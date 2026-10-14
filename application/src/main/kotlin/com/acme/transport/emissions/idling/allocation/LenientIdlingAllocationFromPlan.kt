package com.acme.transport.emissions.idling.allocation

import com.acme.transport.emissions.idling.review.PendingIdlingPlan
import com.acme.transport.emissions.idling.review.normalizeIdlingScheduleCode

fun summarizePendingIdlingPlansForAllocation(items: MutableList<PendingIdlingPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLenientIdlingAllocationsFromPlan(items: MutableList<PendingIdlingPlan>): MutableList<LenientIdlingAllocation> {
    val out = mutableListOf<LenientIdlingAllocation>()
    for (item in items) {
        val derived = newLenientIdlingAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLenientIdlingAllocation(item: LenientIdlingAllocation): String = normalizeIdlingScheduleCode(item.code)
