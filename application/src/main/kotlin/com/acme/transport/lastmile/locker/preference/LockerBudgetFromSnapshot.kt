package com.acme.transport.lastmile.locker.preference

import com.acme.transport.lastmile.drop.exception.DropSnapshot
import com.acme.transport.lastmile.drop.exception.dropPreferenceWeight

fun summarizeDropSnapshotsForBudget(items: MutableList<DropSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLockerBudgetsFromSnapshot(items: MutableList<DropSnapshot>): MutableList<LockerBudget> {
    val out = mutableListOf<LockerBudget>()
    for (item in items) {
        val derived = newLockerBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveLockerBudget(item: LockerBudget): Double = dropPreferenceWeight(4, true)
