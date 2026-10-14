package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.linehaul.backhaul.batch.BackhaulProfile
import com.acme.transport.linehaul.backhaul.batch.backhaulProfileBucket

fun summarizeBackhaulProfilesForRequest(items: MutableList<BackhaulProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveBackhaulRequestsFromProfile(items: MutableList<BackhaulProfile>): MutableList<BackhaulRequest> {
    val out = mutableListOf<BackhaulRequest>()
    for (item in items) {
        val derived = newBackhaulRequest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchBackhaulRequest(item: BackhaulRequest): String = backhaulProfileBucket(item.adjustedTotal())
