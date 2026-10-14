package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.lastmile.redelivery.threshold.RedeliveryEstimate
import com.acme.transport.lastmile.redelivery.threshold.redeliveryEstimateWeight

fun summarizeRedeliveryEstimatesForMilestone(items: MutableList<RedeliveryEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultParcelMilestonesFromEstimate(items: MutableList<RedeliveryEstimate>): MutableList<DefaultParcelMilestone> {
    val out = mutableListOf<DefaultParcelMilestone>()
    for (item in items) {
        val derived = newDefaultParcelMilestone(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDefaultParcelMilestone(item: DefaultParcelMilestone): Double = redeliveryEstimateWeight(2, true)
