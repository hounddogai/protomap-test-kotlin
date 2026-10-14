package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.availability.cycle.BatchedAvailabilityGroup
import com.acme.transport.crewing.availability.cycle.normalizeAvailabilityCycleCode

fun summarizeBatchedAvailabilityGroupsForProfile(items: MutableList<BatchedAvailabilityGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDutyPeriodProfilesFromGroup(items: MutableList<BatchedAvailabilityGroup>): MutableList<DutyPeriodProfile> {
    val out = mutableListOf<DutyPeriodProfile>()
    for (item in items) {
        val derived = newDutyPeriodProfile(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planDutyPeriodProfile(item: DutyPeriodProfile): String = normalizeAvailabilityCycleCode(item.code)
