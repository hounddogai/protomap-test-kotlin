package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.backhaul.milestone.ManualBackhaulQuota
import com.acme.transport.linehaul.backhaul.milestone.classifyLenientBackhaulLedger

fun summarizeManualBackhaulQuotasForBudget(items: MutableList<ManualBackhaulQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRelayBudgetsFromQuota(items: MutableList<ManualBackhaulQuota>): MutableList<RelayBudget> {
    val out = mutableListOf<RelayBudget>()
    for (item in items) {
        val derived = newRelayBudget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRelayBudget(item: RelayBudget): String = classifyLenientBackhaulLedger(item.measuredValue())
