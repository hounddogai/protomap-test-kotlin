package com.acme.transport.safety.coaching.profile

import com.acme.transport.safety.incident.schedule.RegionalIncidentSchedule
import com.acme.transport.safety.incident.schedule.regionalIncidentScheduleBucket

fun summarizeRegionalIncidentSchedulesForProfile(items: MutableList<RegionalIncidentSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCoachingProfilesFromSchedule(items: MutableList<RegionalIncidentSchedule>): MutableList<CoachingProfile> {
    val out = mutableListOf<CoachingProfile>()
    for (item in items) {
        val derived = newCoachingProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectCoachingProfile(item: CoachingProfile): String = regionalIncidentScheduleBucket(item.measuredValue())
