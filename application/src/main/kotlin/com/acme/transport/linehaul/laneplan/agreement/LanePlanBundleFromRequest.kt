package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.linehaul.capacity.request.CapacityRequest
import com.acme.transport.linehaul.capacity.request.normalizeCapacityRecordCode

fun summarizeCapacityRequestsForBundle(items: MutableList<CapacityRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLanePlanBundlesFromRequest(items: MutableList<CapacityRequest>): MutableList<LanePlanBundle> {
    val out = mutableListOf<LanePlanBundle>()
    for (item in items) {
        val derived = newLanePlanBundle(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchLanePlanBundle(item: LanePlanBundle): String = normalizeCapacityRecordCode(item.code)
