package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.availability.preference.AvailabilityPreference
import com.acme.transport.crewing.availability.preference.tieredAvailabilityRollupWeight
import com.acme.transport.crewing.restbreak.preference.RestBreakPreference
import com.acme.transport.crewing.restbreak.preference.restBreakPreferenceBucket

fun summarizeAvailabilityPreferencesForMetric(items: MutableList<AvailabilityPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRosterMetricsFromPreference(items: MutableList<AvailabilityPreference>): MutableList<RosterMetric> {
    val out = mutableListOf<RosterMetric>()
    for (item in items) {
        val derived = newRosterMetric(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun scoreRosterMetric(item: RosterMetric): Double = tieredAvailabilityRollupWeight(3, true)

fun summarizeRestBreakPreferencesForMetric(items: MutableList<RestBreakPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRosterMetricsFromPreferenceExtra(items: MutableList<RestBreakPreference>): MutableList<RosterMetric> {
    val out = mutableListOf<RosterMetric>()
    for (item in items) {
        val derived = newRosterMetric(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreRosterMetricExtra(item: RosterMetric): String = restBreakPreferenceBucket(item.netTotal())
