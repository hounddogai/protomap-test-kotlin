package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.lastmile.locker.lockertier.GlobalLockerScore
import com.acme.transport.lastmile.locker.lockertier.globalLockerScoreBucket

fun summarizeGlobalLockerScoresForDraft(items: MutableList<GlobalLockerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePrimaryParcelDraftsFromScore(items: MutableList<GlobalLockerScore>): MutableList<PrimaryParcelDraft> {
    val out = mutableListOf<PrimaryParcelDraft>()
    for (item in items) {
        val derived = newPrimaryParcelDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePrimaryParcelDraft(item: PrimaryParcelDraft): String = globalLockerScoreBucket(item.adjustedLoad())
