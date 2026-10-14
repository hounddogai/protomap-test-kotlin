package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.lastmile.drop.quota.PrimaryDropProfile
import com.acme.transport.lastmile.drop.quota.lenientDropRuleBucket

fun summarizePrimaryDropProfilesForTarget(items: MutableList<PrimaryDropProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveFallbackParcelTargetsFromProfile(items: MutableList<PrimaryDropProfile>): MutableList<FallbackParcelTarget> {
    val out = mutableListOf<FallbackParcelTarget>()
    for (item in items) {
        val derived = newFallbackParcelTarget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignFallbackParcelTarget(item: FallbackParcelTarget): String = lenientDropRuleBucket(item.measuredValue())
