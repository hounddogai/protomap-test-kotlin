package com.acme.transport.customs.broker.allocation

import com.acme.transport.customs.hold.schedule.DefaultHoldSchedule
import com.acme.transport.customs.hold.schedule.classifyHoldSchedule

fun summarizeDefaultHoldSchedulesForAllocation(items: MutableList<DefaultHoldSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveBrokerAllocationsFromSchedule(items: MutableList<DefaultHoldSchedule>): MutableList<BrokerAllocation> {
    val out = mutableListOf<BrokerAllocation>()
    for (item in items) {
        val derived = newBrokerAllocation(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankBrokerAllocation(item: BrokerAllocation): String = classifyHoldSchedule(item.measuredValue())
