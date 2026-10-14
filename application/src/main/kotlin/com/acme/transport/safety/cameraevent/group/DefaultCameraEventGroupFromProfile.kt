package com.acme.transport.safety.cameraevent.group

import com.acme.transport.safety.coaching.profile.CoachingProfile
import com.acme.transport.safety.coaching.profile.classifyAdaptiveCoachingCycle

fun summarizeCoachingProfilesForGroup(items: MutableList<CoachingProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultCameraEventGroupsFromProfile(items: MutableList<CoachingProfile>): MutableList<DefaultCameraEventGroup> {
    val out = mutableListOf<DefaultCameraEventGroup>()
    for (item in items) {
        val derived = newDefaultCameraEventGroup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun assignDefaultCameraEventGroup(item: DefaultCameraEventGroup): String {
    return classifyAdaptiveCoachingCycle(item.measuredValue())
}
