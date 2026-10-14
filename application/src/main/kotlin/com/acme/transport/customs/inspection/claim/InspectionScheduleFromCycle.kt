package com.acme.transport.customs.inspection.claim

import com.acme.transport.customs.broker.review.BrokerCycle
import com.acme.transport.customs.broker.review.classifyRegionalBrokerRequest

fun summarizeBrokerCyclesForSchedule(items: MutableList<BrokerCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveInspectionSchedulesFromCycle(items: MutableList<BrokerCycle>): MutableList<InspectionSchedule> {
    val out = mutableListOf<InspectionSchedule>()
    for (item in items) {
        val derived = newInspectionSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileInspectionSchedule(item: InspectionSchedule): String {
    return classifyRegionalBrokerRequest(item.adjustedTotal())
}
