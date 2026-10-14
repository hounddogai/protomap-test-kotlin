package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.redelivery.milestone.RedeliverySchedule
import com.acme.transport.lastmile.redelivery.milestone.classifyRedeliveryReservation

fun summarizeRedeliverySchedulesForAdjustment(items: MutableList<RedeliverySchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAttemptAdjustmentsFromSchedule(items: MutableList<RedeliverySchedule>): MutableList<AttemptAdjustment> {
    val out = mutableListOf<AttemptAdjustment>()
    for (item in items) {
        val derived = newAttemptAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateAttemptAdjustment(item: AttemptAdjustment): String {
    return classifyRedeliveryReservation(item.adjustedLoad())
}
