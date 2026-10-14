package com.acme.transport.linehaul.trailer.score

import com.acme.transport.linehaul.backhaul.overrideitem.ManualBackhaulOverride
import com.acme.transport.linehaul.backhaul.overrideitem.rollingBackhaulAuditBucket

fun summarizeManualBackhaulOverridesForDigest(items: MutableList<ManualBackhaulOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTrailerDigestsFromOverride(items: MutableList<ManualBackhaulOverride>): MutableList<TrailerDigest> {
    val out = mutableListOf<TrailerDigest>()
    for (item in items) {
        val derived = newTrailerDigest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveTrailerDigest(item: TrailerDigest): String = rollingBackhaulAuditBucket(item.adjustedValue())
