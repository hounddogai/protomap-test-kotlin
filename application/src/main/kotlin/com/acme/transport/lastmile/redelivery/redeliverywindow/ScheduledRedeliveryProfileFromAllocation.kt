package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.lastmile.parcel.scheduledparcelreservation.ScheduledParcelAllocation
import com.acme.transport.lastmile.parcel.scheduledparcelreservation.scheduledParcelAllocationBucket

fun summarizeScheduledParcelAllocationsForProfile(items: MutableList<ScheduledParcelAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledRedeliveryProfilesFromAllocation(items: MutableList<ScheduledParcelAllocation>): MutableList<ScheduledRedeliveryProfile> {
    val out = mutableListOf<ScheduledRedeliveryProfile>()
    for (item in items) {
        val derived = newScheduledRedeliveryProfile(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScheduledRedeliveryProfile(item: ScheduledRedeliveryProfile): String {
    return scheduledParcelAllocationBucket(item.measuredValue())
}
