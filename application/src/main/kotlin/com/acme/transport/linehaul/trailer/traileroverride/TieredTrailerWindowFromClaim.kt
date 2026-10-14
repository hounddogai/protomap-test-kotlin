package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.linehaul.laneplan.plansegment.LanePlanClaim
import com.acme.transport.linehaul.laneplan.plansegment.classifyLanePlanClaim

fun summarizeLanePlanClaimsForWindow(items: MutableList<LanePlanClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTieredTrailerWindowsFromClaim(items: MutableList<LanePlanClaim>): MutableList<TieredTrailerWindow> {
    val out = mutableListOf<TieredTrailerWindow>()
    for (item in items) {
        val derived = newTieredTrailerWindow(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveTieredTrailerWindow(item: TieredTrailerWindow): String = classifyLanePlanClaim(item.adjustedExposure())
