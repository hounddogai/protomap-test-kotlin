package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.ratecard.cycle.RateCardSummary
import com.acme.transport.freightpay.ratecard.cycle.classifyRateCardCycle

fun summarizeRateCardSummariesForBundle(items: MutableList<RateCardSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDisputeBundlesFromSummary(items: MutableList<RateCardSummary>): MutableList<DisputeBundle> {
    val out = mutableListOf<DisputeBundle>()
    for (item in items) {
        val derived = newDisputeBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDisputeBundle(item: DisputeBundle): String = classifyRateCardCycle(item.adjustedValue())
