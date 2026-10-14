package com.acme.transport.crewing.roster.milestone

import com.acme.transport.crewing.dutyperiod.bundle.FallbackDutyPeriodBundle
import com.acme.transport.crewing.dutyperiod.bundle.classifyDutyPeriodWindow

fun summarizeFallbackDutyPeriodBundlesForMilestone(items: MutableList<FallbackDutyPeriodBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRosterMilestonesFromBundle(items: MutableList<FallbackDutyPeriodBundle>): MutableList<RosterMilestone> {
    val out = mutableListOf<RosterMilestone>()
    for (item in items) {
        val derived = newRosterMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRosterMilestone(item: RosterMilestone): String = classifyDutyPeriodWindow(item.weightedExposure())
