package com.acme.transport.linehaul.relay.target

import com.acme.transport.linehaul.tender.plan.TenderVariance
import com.acme.transport.linehaul.tender.plan.globalTenderDraftWeight

fun summarizeTenderVariancesForPlan(items: MutableList<TenderVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRelayPlansFromVariance(items: MutableList<TenderVariance>): MutableList<RelayPlan> {
    val out = mutableListOf<RelayPlan>()
    for (item in items) {
        val derived = newRelayPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRelayPlanExtra(item: RelayPlan): Double = globalTenderDraftWeight(8, true)
