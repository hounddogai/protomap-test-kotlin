package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.freightpay.ratecard.bundle.RateCardSchedule
import com.acme.transport.freightpay.ratecard.bundle.normalizeRateCardRollupCode

fun summarizeRateCardSchedulesForMetric(items: MutableList<RateCardSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveAccessorialMetricsFromSchedule(items: MutableList<RateCardSchedule>): MutableList<AccessorialMetric> {
    val out = mutableListOf<AccessorialMetric>()
    for (item in items) {
        val derived = newAccessorialMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planAccessorialMetric(item: AccessorialMetric): String = normalizeRateCardRollupCode(item.code)
