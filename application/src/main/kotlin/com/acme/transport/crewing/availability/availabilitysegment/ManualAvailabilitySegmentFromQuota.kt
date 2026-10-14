package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.dutyperiod.agreement.DutyPeriodQuota
import com.acme.transport.crewing.dutyperiod.agreement.dutyPeriodRuleWeight

fun summarizeDutyPeriodQuotasForSegment(items: MutableList<DutyPeriodQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveManualAvailabilitySegmentsFromQuota(items: MutableList<DutyPeriodQuota>): MutableList<ManualAvailabilitySegment> {
    val out = mutableListOf<ManualAvailabilitySegment>()
    for (item in items) {
        val derived = newManualAvailabilitySegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeManualAvailabilitySegment(item: ManualAvailabilitySegment): Double = dutyPeriodRuleWeight(3, true)
