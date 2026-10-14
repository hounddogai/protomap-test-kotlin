package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.broker.tier.ScheduledBrokerScore
import com.acme.transport.customs.broker.tier.normalizePrimaryBrokerTierCode

fun summarizeScheduledBrokerScoresForSchedule(items: MutableList<ScheduledBrokerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveBatchedHoldSchedulesFromScore(items: MutableList<ScheduledBrokerScore>): MutableList<BatchedHoldSchedule> {
    val out = mutableListOf<BatchedHoldSchedule>()
    for (item in items) {
        val derived = newBatchedHoldSchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleBatchedHoldSchedule(item: BatchedHoldSchedule): String = normalizePrimaryBrokerTierCode(item.code)
