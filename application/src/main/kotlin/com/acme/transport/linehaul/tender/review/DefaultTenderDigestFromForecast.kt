package com.acme.transport.linehaul.tender.review

import com.acme.transport.linehaul.tender.segment.GlobalTenderForecast
import com.acme.transport.linehaul.tender.segment.classifyGlobalTenderForecast

fun summarizeGlobalTenderForecastsForDigest(items: MutableList<GlobalTenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveDefaultTenderDigestsFromForecast(items: MutableList<GlobalTenderForecast>): MutableList<DefaultTenderDigest> {
    val out = mutableListOf<DefaultTenderDigest>()
    for (item in items) {
        val derived = newDefaultTenderDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultTenderDigest(item: DefaultTenderDigest): String = classifyGlobalTenderForecast(item.netExposure())
