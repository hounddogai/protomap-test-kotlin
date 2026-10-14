package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.freightpay.accessorial.accessorialmetric.LenientAccessorialMetric
import com.acme.transport.freightpay.accessorial.accessorialmetric.classifyAdaptiveAccessorialRequest

fun summarizeLenientAccessorialMetricsForProposal(items: MutableList<LenientAccessorialMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardProposalsFromMetric(items: MutableList<LenientAccessorialMetric>): MutableList<RateCardProposal> {
    val out = mutableListOf<RateCardProposal>()
    for (item in items) {
        val derived = newRateCardProposal(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRateCardProposal(item: RateCardProposal): String = classifyAdaptiveAccessorialRequest(item.netTotal())
