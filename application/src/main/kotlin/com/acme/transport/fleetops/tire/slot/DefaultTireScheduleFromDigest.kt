package com.acme.transport.fleetops.tire.slot

import com.acme.transport.fleetops.serviceplan.planmetric.GlobalServicePlanDigest
import com.acme.transport.fleetops.serviceplan.planmetric.servicePlanWindowWeight

fun summarizeGlobalServicePlanDigestsForSchedule(items: MutableList<GlobalServicePlanDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultTireSchedulesFromDigest(items: MutableList<GlobalServicePlanDigest>): MutableList<DefaultTireSchedule> {
    val out = mutableListOf<DefaultTireSchedule>()
    for (item in items) {
        val derived = newDefaultTireSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planDefaultTireSchedule(item: DefaultTireSchedule): Double = servicePlanWindowWeight(1, true)
