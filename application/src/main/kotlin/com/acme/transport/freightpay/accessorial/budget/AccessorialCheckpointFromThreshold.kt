package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.freightpay.ratecard.request.RateCardThreshold
import com.acme.transport.freightpay.ratecard.request.normalizeRateCardRequestCode

fun summarizeRateCardThresholdsForCheckpoint(items: MutableList<RateCardThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAccessorialCheckpointsFromThreshold(items: MutableList<RateCardThreshold>): MutableList<AccessorialCheckpoint> {
    val out = mutableListOf<AccessorialCheckpoint>()
    for (item in items) {
        val derived = newAccessorialCheckpoint(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAccessorialCheckpoint(item: AccessorialCheckpoint): String = normalizeRateCardRequestCode(item.code)
