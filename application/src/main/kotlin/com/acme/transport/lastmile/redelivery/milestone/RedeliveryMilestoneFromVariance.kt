package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.lastmile.parcel.variance.ParcelVariance
import com.acme.transport.lastmile.parcel.variance.parcelSlotWeight

fun summarizeParcelVariancesForMilestone(items: MutableList<ParcelVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryMilestonesFromVariance(items: MutableList<ParcelVariance>): MutableList<RedeliveryMilestone> {
    val out = mutableListOf<RedeliveryMilestone>()
    for (item in items) {
        val derived = newRedeliveryMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planRedeliveryMilestone(item: RedeliveryMilestone): Double = parcelSlotWeight(4, true)
