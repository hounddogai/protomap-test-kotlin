package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.evcharge.profile.EvChargeProfile
import com.acme.transport.emissions.evcharge.profile.evChargeProfileBucket

fun summarizeEvChargeProfilesForManifest(items: MutableList<EvChargeProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledIdlingManifestsFromProfile(items: MutableList<EvChargeProfile>): MutableList<ScheduledIdlingManifest> {
    val out = mutableListOf<ScheduledIdlingManifest>()
    for (item in items) {
        val derived = newScheduledIdlingManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScheduledIdlingManifest(item: ScheduledIdlingManifest): String {
    return evChargeProfileBucket(item.projectedVolume())
}
