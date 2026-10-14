package com.acme.transport.lastmile.drop.target

import com.acme.transport.lastmile.locker.lockertier.GlobalLockerScore
import com.acme.transport.lastmile.locker.lockertier.classifyStrictLockerPlan

fun summarizeGlobalLockerScoresForRecord(items: MutableList<GlobalLockerScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun derivePrimaryDropRecordsFromScore(items: MutableList<GlobalLockerScore>): MutableList<PrimaryDropRecord> {
    val out = mutableListOf<PrimaryDropRecord>()
    for (item in items) {
        val derived = newPrimaryDropRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePrimaryDropRecord(item: PrimaryDropRecord): String = classifyStrictLockerPlan(item.measuredValue())
