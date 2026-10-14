package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.lastmile.attempt.metric.LegacyAttemptCycle
import com.acme.transport.lastmile.attempt.metric.normalizeLegacyAttemptCycleCode

fun summarizeLegacyAttemptCyclesForForecast(items: MutableList<LegacyAttemptCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultRedeliveryForecastsFromCycle(items: MutableList<LegacyAttemptCycle>): MutableList<DefaultRedeliveryForecast> {
    val out = mutableListOf<DefaultRedeliveryForecast>()
    for (item in items) {
        val derived = newDefaultRedeliveryForecast(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeDefaultRedeliveryForecast(item: DefaultRedeliveryForecast): String {
    return normalizeLegacyAttemptCycleCode(item.code)
}
