package com.acme.transport.network.transittime.rollup

import com.acme.transport.network.servicearea.target.ServiceAreaRule
import com.acme.transport.network.servicearea.target.normalizeServiceAreaRuleCode

fun summarizeServiceAreaRulesForManifest(items: MutableList<ServiceAreaRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledTransitTimeManifestsFromRule(items: MutableList<ServiceAreaRule>): MutableList<ScheduledTransitTimeManifest> {
    val out = mutableListOf<ScheduledTransitTimeManifest>()
    for (item in items) {
        val derived = newScheduledTransitTimeManifest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignScheduledTransitTimeManifest(item: ScheduledTransitTimeManifest): String {
    return normalizeServiceAreaRuleCode(item.code)
}
