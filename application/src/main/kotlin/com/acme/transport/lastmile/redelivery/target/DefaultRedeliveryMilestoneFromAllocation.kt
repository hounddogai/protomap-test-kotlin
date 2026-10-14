package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.lastmile.parcel.allocation.ParcelAllocation
import com.acme.transport.lastmile.parcel.allocation.normalizeParcelAllocationCode

fun summarizeParcelAllocationsForMilestone(items: MutableList<ParcelAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveDefaultRedeliveryMilestonesFromAllocation(items: MutableList<ParcelAllocation>): MutableList<DefaultRedeliveryMilestone> {
    val out = mutableListOf<DefaultRedeliveryMilestone>()
    for (item in items) {
        val derived = newDefaultRedeliveryMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultRedeliveryMilestone(item: DefaultRedeliveryMilestone): String {
    return normalizeParcelAllocationCode(item.code)
}
