package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.crewing.roster.rosterforecast.PendingRosterManifest
import com.acme.transport.crewing.roster.rosterforecast.pendingRosterManifestBucket

fun summarizePendingRosterManifestsForGroup(items: MutableList<PendingRosterManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDutyPeriodGroupsFromManifest(items: MutableList<PendingRosterManifest>): MutableList<DutyPeriodGroup> {
    val out = mutableListOf<DutyPeriodGroup>()
    for (item in items) {
        val derived = newDutyPeriodGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchDutyPeriodGroup(item: DutyPeriodGroup): String = pendingRosterManifestBucket(item.adjustedLoad())
