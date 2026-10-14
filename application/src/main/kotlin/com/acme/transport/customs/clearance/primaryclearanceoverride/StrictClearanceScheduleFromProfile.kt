package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.hold.holdmapping.ManualHoldProfile
import com.acme.transport.customs.hold.holdmapping.scheduledHoldMappingBucket

fun summarizeManualHoldProfilesForSchedule(items: MutableList<ManualHoldProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveStrictClearanceSchedulesFromProfile(items: MutableList<ManualHoldProfile>): MutableList<StrictClearanceSchedule> {
    val out = mutableListOf<StrictClearanceSchedule>()
    for (item in items) {
        val derived = newStrictClearanceSchedule(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeStrictClearanceSchedule(item: StrictClearanceSchedule): String {
    return scheduledHoldMappingBucket(item.adjustedExposure())
}
