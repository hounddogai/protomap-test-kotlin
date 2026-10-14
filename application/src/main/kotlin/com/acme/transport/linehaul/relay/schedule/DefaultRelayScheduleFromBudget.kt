package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.linehaul.relay.score.LegacyRelayBudget
import com.acme.transport.linehaul.relay.score.rollingRelayScoreWeight

fun summarizeLegacyRelayBudgetsForSchedule(items: MutableList<LegacyRelayBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDefaultRelaySchedulesFromBudget(items: MutableList<LegacyRelayBudget>): MutableList<DefaultRelaySchedule> {
    val out = mutableListOf<DefaultRelaySchedule>()
    for (item in items) {
        val derived = newDefaultRelaySchedule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDefaultRelaySchedule(item: DefaultRelaySchedule): Double = rollingRelayScoreWeight(3, true)
