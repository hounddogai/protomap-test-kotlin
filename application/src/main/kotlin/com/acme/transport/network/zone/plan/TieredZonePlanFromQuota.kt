package com.acme.transport.network.zone.plan

import com.acme.transport.network.hub.legacyhubplan.ManualHubQuota
import com.acme.transport.network.hub.legacyhubplan.normalizeManualHubQuotaCode

fun summarizeManualHubQuotasForPlan(items: MutableList<ManualHubQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveTieredZonePlansFromQuota(items: MutableList<ManualHubQuota>): MutableList<TieredZonePlan> {
    val out = mutableListOf<TieredZonePlan>()
    for (item in items) {
        val derived = newTieredZonePlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTieredZonePlan(item: TieredZonePlan): String = normalizeManualHubQuotaCode(item.code)
