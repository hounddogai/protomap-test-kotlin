package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.crewing.restbreak.policy.ScheduledRestBreakLedger
import com.acme.transport.crewing.restbreak.policy.restBreakPolicyBucket

fun summarizeScheduledRestBreakLedgersForBudget(items: MutableList<ScheduledRestBreakLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveCappedAvailabilityBudgetsFromLedger(items: MutableList<ScheduledRestBreakLedger>): MutableList<CappedAvailabilityBudget> {
    val out = mutableListOf<CappedAvailabilityBudget>()
    for (item in items) {
        val derived = newCappedAvailabilityBudget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planCappedAvailabilityBudget(item: CappedAvailabilityBudget): String {
    return restBreakPolicyBucket(item.netExposure())
}
