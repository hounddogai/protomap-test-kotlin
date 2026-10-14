package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.availability.availabilitytier.AvailabilityMilestone
import com.acme.transport.crewing.availability.availabilitytier.classifyPendingAvailabilityTier

fun summarizeAvailabilityMilestonesForSnapshot(items: MutableList<AvailabilityMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDefaultDutyPeriodSnapshotsFromMilestone(items: MutableList<AvailabilityMilestone>): MutableList<DefaultDutyPeriodSnapshot> {
    val out = mutableListOf<DefaultDutyPeriodSnapshot>()
    for (item in items) {
        val derived = newDefaultDutyPeriodSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchDefaultDutyPeriodSnapshot(item: DefaultDutyPeriodSnapshot): String {
    return classifyPendingAvailabilityTier(item.netExposure())
}
