package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.freightpay.accessorial.cycle.AccessorialCycle
import com.acme.transport.freightpay.accessorial.cycle.accessorialReviewWeight

fun summarizeAccessorialCyclesForSummary(items: MutableList<AccessorialCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRollingRateCardSummariesFromCycle(items: MutableList<AccessorialCycle>): MutableList<RollingRateCardSummary> {
    val out = mutableListOf<RollingRateCardSummary>()
    for (item in items) {
        val derived = newRollingRateCardSummary(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyRollingRateCardSummary(item: RollingRateCardSummary): Double = accessorialReviewWeight(4, true)
