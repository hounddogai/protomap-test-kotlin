package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.lastmile.drop.milestone.DropSegment
import com.acme.transport.lastmile.drop.milestone.dropMilestoneWeight

fun summarizeDropSegmentsForOverride(items: MutableList<DropSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLockerOverridesFromSegment(items: MutableList<DropSegment>): MutableList<LockerOverride> {
    val out = mutableListOf<LockerOverride>()
    for (item in items) {
        val derived = newLockerOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceLockerOverride(item: LockerOverride): Double = dropMilestoneWeight(3, true)
