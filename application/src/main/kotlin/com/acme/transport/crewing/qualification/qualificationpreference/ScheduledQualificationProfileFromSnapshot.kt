package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.crewing.restbreak.segment.GlobalRestBreakSnapshot
import com.acme.transport.crewing.restbreak.segment.normalizeGlobalRestBreakSegmentCode

fun summarizeGlobalRestBreakSnapshotsForProfile(items: MutableList<GlobalRestBreakSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledQualificationProfilesFromSnapshot(items: MutableList<GlobalRestBreakSnapshot>): MutableList<ScheduledQualificationProfile> {
    val out = mutableListOf<ScheduledQualificationProfile>()
    for (item in items) {
        val derived = newScheduledQualificationProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledQualificationProfile(item: ScheduledQualificationProfile): String {
    return normalizeGlobalRestBreakSegmentCode(item.code)
}
