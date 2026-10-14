package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.availability.exception.AvailabilityException
import com.acme.transport.crewing.availability.exception.batchedAvailabilityAgreementWeight

fun summarizeAvailabilityExceptionsForBudget(items: MutableList<AvailabilityException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledRestBreakBudgetsFromException(items: MutableList<AvailabilityException>): MutableList<ScheduledRestBreakBudget> {
    val out = mutableListOf<ScheduledRestBreakBudget>()
    for (item in items) {
        val derived = newScheduledRestBreakBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateScheduledRestBreakBudget(item: ScheduledRestBreakBudget): Double {
    return batchedAvailabilityAgreementWeight(5, true)
}
