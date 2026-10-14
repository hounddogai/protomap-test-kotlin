package com.acme.transport.network.hub.summary

import com.acme.transport.network.servicearea.target.ServiceAreaTarget
import com.acme.transport.network.servicearea.target.normalizeServiceAreaRuleCode

fun summarizeServiceAreaTargetsForAudit(items: MutableList<ServiceAreaTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedHubAuditsFromTarget(items: MutableList<ServiceAreaTarget>): MutableList<WeightedHubAudit> {
    val out = mutableListOf<WeightedHubAudit>()
    for (item in items) {
        val derived = newWeightedHubAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateWeightedHubAudit(item: WeightedHubAudit): String = normalizeServiceAreaRuleCode(item.code)
