package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.restbreak.agreement.RestBreakEstimate
import com.acme.transport.crewing.restbreak.agreement.restBreakQuotaWeight

fun summarizeRestBreakEstimatesForRollup(items: MutableList<RestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveAvailabilityRollupsFromEstimate(items: MutableList<RestBreakEstimate>): MutableList<AvailabilityRollup> {
    val out = mutableListOf<AvailabilityRollup>()
    for (item in items) {
        val derived = newAvailabilityRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleAvailabilityRollup(item: AvailabilityRollup): Double = restBreakQuotaWeight(5, true)
