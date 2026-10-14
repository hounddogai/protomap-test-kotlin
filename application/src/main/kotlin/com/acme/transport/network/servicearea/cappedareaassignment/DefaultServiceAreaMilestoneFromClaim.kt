package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.network.servicearea.weightedareaclaim.AdaptiveServiceAreaClaim
import com.acme.transport.network.servicearea.weightedareaclaim.lenientServiceAreaScoreWeight

fun summarizeAdaptiveServiceAreaClaimsForMilestone(items: MutableList<AdaptiveServiceAreaClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultServiceAreaMilestonesFromClaim(items: MutableList<AdaptiveServiceAreaClaim>): MutableList<DefaultServiceAreaMilestone> {
    val out = mutableListOf<DefaultServiceAreaMilestone>()
    for (item in items) {
        val derived = newDefaultServiceAreaMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDefaultServiceAreaMilestone(item: DefaultServiceAreaMilestone): Double {
    return lenientServiceAreaScoreWeight(6, true)
}
