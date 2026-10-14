package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.qualification.schedule.TieredQualificationLedger
import com.acme.transport.crewing.qualification.schedule.batchedQualificationMilestoneWeight

fun summarizeTieredQualificationLedgersForGroup(items: MutableList<TieredQualificationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRollingAvailabilityGroupsFromLedger(items: MutableList<TieredQualificationLedger>): MutableList<RollingAvailabilityGroup> {
    val out = mutableListOf<RollingAvailabilityGroup>()
    for (item in items) {
        val derived = newRollingAvailabilityGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRollingAvailabilityGroup(item: RollingAvailabilityGroup): Double {
    return batchedQualificationMilestoneWeight(9, true)
}
