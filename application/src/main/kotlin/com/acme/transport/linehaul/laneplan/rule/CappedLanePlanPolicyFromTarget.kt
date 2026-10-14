package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.linehaul.backhaul.backhaultarget.StrictBackhaulTarget
import com.acme.transport.linehaul.backhaul.backhaultarget.classifyPendingBackhaulForecast

fun summarizeStrictBackhaulTargetsForPolicy(items: MutableList<StrictBackhaulTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCappedLanePlanPoliciesFromTarget(items: MutableList<StrictBackhaulTarget>): MutableList<CappedLanePlanPolicy> {
    val out = mutableListOf<CappedLanePlanPolicy>()
    for (item in items) {
        val derived = newCappedLanePlanPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedLanePlanPolicy(item: CappedLanePlanPolicy): String {
    return classifyPendingBackhaulForecast(item.adjustedLoad())
}
