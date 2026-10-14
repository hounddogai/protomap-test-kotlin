package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.safety.incident.segment.IncidentException
import com.acme.transport.safety.incident.segment.incidentSegmentBucket

fun summarizeIncidentExceptionsForMilestone(items: MutableList<IncidentException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveNearMissMilestonesFromException(items: MutableList<IncidentException>): MutableList<NearMissMilestone> {
    val out = mutableListOf<NearMissMilestone>()
    for (item in items) {
        val derived = newNearMissMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateNearMissMilestone(item: NearMissMilestone): String = incidentSegmentBucket(item.projectedExposure())
