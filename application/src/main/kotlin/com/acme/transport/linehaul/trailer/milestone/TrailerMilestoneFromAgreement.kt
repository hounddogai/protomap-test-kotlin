package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.linehaul.capacity.capacitymapping.ScheduledCapacityAgreement
import com.acme.transport.linehaul.capacity.capacitymapping.classifyCapacityForecast

fun summarizeScheduledCapacityAgreementsForMilestone(items: MutableList<ScheduledCapacityAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTrailerMilestonesFromAgreement(items: MutableList<ScheduledCapacityAgreement>): MutableList<TrailerMilestone> {
    val out = mutableListOf<TrailerMilestone>()
    for (item in items) {
        val derived = newTrailerMilestone(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTrailerMilestone(item: TrailerMilestone): String = classifyCapacityForecast(item.netVolume())
