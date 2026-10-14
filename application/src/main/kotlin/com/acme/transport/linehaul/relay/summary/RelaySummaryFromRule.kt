package com.acme.transport.linehaul.relay.summary

import com.acme.transport.linehaul.backhaul.backhaulthreshold.BackhaulRule
import com.acme.transport.linehaul.backhaul.backhaulthreshold.regionalBackhaulLedgerWeight

fun summarizeBackhaulRulesForSummary(items: MutableList<BackhaulRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRelaySummariesFromRule(items: MutableList<BackhaulRule>): MutableList<RelaySummary> {
    val out = mutableListOf<RelaySummary>()
    for (item in items) {
        val derived = newRelaySummary(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankRelaySummary(item: RelaySummary): Double = regionalBackhaulLedgerWeight(6, true)
