package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.availability.audit.AvailabilityAudit
import com.acme.transport.crewing.availability.audit.legacyAvailabilityGroupBucket

fun summarizeAvailabilityAuditsForLedger(items: MutableList<AvailabilityAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDutyPeriodLedgersFromAudit(items: MutableList<AvailabilityAudit>): MutableList<DutyPeriodLedger> {
    val out = mutableListOf<DutyPeriodLedger>()
    for (item in items) {
        val derived = newDutyPeriodLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDutyPeriodLedger(item: DutyPeriodLedger): String {
    return legacyAvailabilityGroupBucket(item.measuredValue())
}
