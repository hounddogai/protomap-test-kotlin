package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.availability.availabilityaudit.AdaptiveAvailabilityAudit
import com.acme.transport.crewing.availability.availabilityaudit.availabilitySummaryBucket

fun summarizeAdaptiveAvailabilityAuditsForAgreement(items: MutableList<AdaptiveAvailabilityAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveBatchedDutyPeriodAgreementsFromAudit(items: MutableList<AdaptiveAvailabilityAudit>): MutableList<BatchedDutyPeriodAgreement> {
    val out = mutableListOf<BatchedDutyPeriodAgreement>()
    for (item in items) {
        val derived = newBatchedDutyPeriodAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignBatchedDutyPeriodAgreement(item: BatchedDutyPeriodAgreement): String {
    return availabilitySummaryBucket(item.measuredValue())
}
