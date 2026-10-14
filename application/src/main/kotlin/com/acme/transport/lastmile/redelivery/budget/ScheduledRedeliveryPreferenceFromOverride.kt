package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.lastmile.drop.dropdraft.DropOverride
import com.acme.transport.lastmile.drop.dropdraft.regionalDropDraftWeight

fun summarizeDropOverridesForPreference(items: MutableList<DropOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveScheduledRedeliveryPreferencesFromOverride(items: MutableList<DropOverride>): MutableList<ScheduledRedeliveryPreference> {
    val out = mutableListOf<ScheduledRedeliveryPreference>()
    for (item in items) {
        val derived = newScheduledRedeliveryPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveScheduledRedeliveryPreference(item: ScheduledRedeliveryPreference): Double {
    return regionalDropDraftWeight(7, true)
}
