package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.lastmile.locker.mapping.LockerMapping
import com.acme.transport.lastmile.locker.mapping.normalizeWeightedLockerExceptionCode

fun summarizeLockerMappingsForPreference(items: MutableList<LockerMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryRedeliveryPreferencesFromMapping(items: MutableList<LockerMapping>): MutableList<PrimaryRedeliveryPreference> {
    val out = mutableListOf<PrimaryRedeliveryPreference>()
    for (item in items) {
        val derived = newPrimaryRedeliveryPreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergePrimaryRedeliveryPreference(item: PrimaryRedeliveryPreference): String {
    return normalizeWeightedLockerExceptionCode(item.code)
}
