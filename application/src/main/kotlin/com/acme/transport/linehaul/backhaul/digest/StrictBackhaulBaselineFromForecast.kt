package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.linehaul.relay.relaythreshold.RelayForecast
import com.acme.transport.linehaul.relay.relaythreshold.classifyRegionalRelayThreshold

fun summarizeRelayForecastsForBaseline(items: MutableList<RelayForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveStrictBackhaulBaselinesFromForecast(items: MutableList<RelayForecast>): MutableList<StrictBackhaulBaseline> {
    val out = mutableListOf<StrictBackhaulBaseline>()
    for (item in items) {
        val derived = newStrictBackhaulBaseline(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planStrictBackhaulBaseline(item: StrictBackhaulBaseline): String {
    return classifyRegionalRelayThreshold(item.effectiveValue())
}
