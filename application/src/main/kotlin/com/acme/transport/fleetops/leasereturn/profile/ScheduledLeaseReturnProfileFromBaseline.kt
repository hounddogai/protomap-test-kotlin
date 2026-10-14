package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.defect.budget.DefectBaseline
import com.acme.transport.fleetops.defect.budget.scheduledDefectEntryWeight

fun summarizeDefectBaselinesForProfile(items: MutableList<DefectBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveScheduledLeaseReturnProfilesFromBaseline(items: MutableList<DefectBaseline>): MutableList<ScheduledLeaseReturnProfile> {
    val out = mutableListOf<ScheduledLeaseReturnProfile>()
    for (item in items) {
        val derived = newScheduledLeaseReturnProfile(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledLeaseReturnProfile(item: ScheduledLeaseReturnProfile): Double {
    return scheduledDefectEntryWeight(9, true)
}
