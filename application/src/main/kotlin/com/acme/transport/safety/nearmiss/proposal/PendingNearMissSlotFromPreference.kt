package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.safety.nearmiss.preference.NearMissPreference
import com.acme.transport.safety.nearmiss.preference.manualNearMissProfileWeight

fun summarizeNearMissPreferencesForSlot(items: MutableList<NearMissPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingNearMissSlotsFromPreference(items: MutableList<NearMissPreference>): MutableList<PendingNearMissSlot> {
    val out = mutableListOf<PendingNearMissSlot>()
    for (item in items) {
        val derived = newPendingNearMissSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePendingNearMissSlot(item: PendingNearMissSlot): Double = manualNearMissProfileWeight(7, true)
