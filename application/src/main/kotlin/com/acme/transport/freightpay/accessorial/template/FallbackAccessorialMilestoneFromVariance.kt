package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.accessorial.accessorialentry.FallbackAccessorialVariance
import com.acme.transport.freightpay.accessorial.accessorialentry.accessorialSlotBucket

fun summarizeFallbackAccessorialVariancesForMilestone(items: MutableList<FallbackAccessorialVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveFallbackAccessorialMilestonesFromVariance(items: MutableList<FallbackAccessorialVariance>): MutableList<FallbackAccessorialMilestone> {
    val out = mutableListOf<FallbackAccessorialMilestone>()
    for (item in items) {
        val derived = newFallbackAccessorialMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignFallbackAccessorialMilestone(item: FallbackAccessorialMilestone): String {
    return accessorialSlotBucket(item.projectedLoad())
}
