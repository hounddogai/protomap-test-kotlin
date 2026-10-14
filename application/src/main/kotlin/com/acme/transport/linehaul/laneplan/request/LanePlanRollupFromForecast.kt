package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.linehaul.backhaul.template.BackhaulForecast
import com.acme.transport.linehaul.backhaul.template.normalizeBackhaulForecastCode

fun summarizeBackhaulForecastsForRollup(items: MutableList<BackhaulForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveLanePlanRollupsFromForecast(items: MutableList<BackhaulForecast>): MutableList<LanePlanRollup> {
    val out = mutableListOf<LanePlanRollup>()
    for (item in items) {
        val derived = newLanePlanRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLanePlanRollup(item: LanePlanRollup): String = normalizeBackhaulForecastCode(item.code)
