package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.drop.cappeddropmilestone.DropSchedule
import com.acme.transport.lastmile.drop.cappeddropmilestone.classifyPrimaryDropEntry

fun summarizeDropSchedulesForNotice(items: MutableList<DropSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredDropNoticesFromSchedule(items: MutableList<DropSchedule>): MutableList<TieredDropNotice> {
    val out = mutableListOf<TieredDropNotice>()
    for (item in items) {
        val derived = newTieredDropNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun applyTieredDropNotice(item: TieredDropNotice): String = classifyPrimaryDropEntry(item.projectedLoad())
