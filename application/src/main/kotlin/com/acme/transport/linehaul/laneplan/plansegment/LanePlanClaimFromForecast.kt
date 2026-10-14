package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.linehaul.tender.segment.GlobalTenderForecast
import com.acme.transport.linehaul.tender.segment.classifyGlobalTenderForecast

fun summarizeGlobalTenderForecastsForClaim(items: MutableList<GlobalTenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLanePlanClaimsFromForecast(items: MutableList<GlobalTenderForecast>): MutableList<LanePlanClaim> {
    val out = mutableListOf<LanePlanClaim>()
    for (item in items) {
        val derived = newLanePlanClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLanePlanClaim(item: LanePlanClaim): String = classifyGlobalTenderForecast(item.measuredValue())
