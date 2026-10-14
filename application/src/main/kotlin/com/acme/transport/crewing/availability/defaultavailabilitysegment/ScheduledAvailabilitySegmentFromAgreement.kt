package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.crewing.swap.agreement.RollingSwapAgreement
import com.acme.transport.crewing.swap.agreement.normalizeRollingSwapAgreementCode

fun summarizeRollingSwapAgreementsForSegment(items: MutableList<RollingSwapAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveScheduledAvailabilitySegmentsFromAgreement(items: MutableList<RollingSwapAgreement>): MutableList<ScheduledAvailabilitySegment> {
    val out = mutableListOf<ScheduledAvailabilitySegment>()
    for (item in items) {
        val derived = newScheduledAvailabilitySegment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchScheduledAvailabilitySegment(item: ScheduledAvailabilitySegment): String {
    return normalizeRollingSwapAgreementCode(item.code)
}
