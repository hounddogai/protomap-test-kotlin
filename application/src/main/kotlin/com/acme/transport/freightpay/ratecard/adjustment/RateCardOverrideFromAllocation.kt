package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.audit.allocation.RegionalAuditAllocation
import com.acme.transport.freightpay.audit.allocation.regionalAuditAllocationWeight

fun summarizeRegionalAuditAllocationsForOverride(items: MutableList<RegionalAuditAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardOverridesFromAllocation(items: MutableList<RegionalAuditAllocation>): MutableList<RateCardOverride> {
    val out = mutableListOf<RateCardOverride>()
    for (item in items) {
        val derived = newRateCardOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyRateCardOverride(item: RateCardOverride): Double = regionalAuditAllocationWeight(8, true)
