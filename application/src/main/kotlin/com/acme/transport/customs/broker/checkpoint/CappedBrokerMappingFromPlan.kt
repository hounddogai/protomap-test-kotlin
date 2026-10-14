package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.tariff.plan.TariffPlan
import com.acme.transport.customs.tariff.plan.tariffPlanWeight

fun summarizeTariffPlansForMapping(items: MutableList<TariffPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCappedBrokerMappingsFromPlan(items: MutableList<TariffPlan>): MutableList<CappedBrokerMapping> {
    val out = mutableListOf<CappedBrokerMapping>()
    for (item in items) {
        val derived = newCappedBrokerMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeCappedBrokerMapping(item: CappedBrokerMapping): Double = tariffPlanWeight(4, true)
