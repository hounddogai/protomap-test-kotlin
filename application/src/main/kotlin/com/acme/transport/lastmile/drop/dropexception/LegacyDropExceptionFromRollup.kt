package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.lastmile.locker.mapping.LockerRollup
import com.acme.transport.lastmile.locker.mapping.weightedLockerExceptionBucket

fun summarizeLockerRollupsForException(items: MutableList<LockerRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLegacyDropExceptionsFromRollup(items: MutableList<LockerRollup>): MutableList<LegacyDropException> {
    val out = mutableListOf<LegacyDropException>()
    for (item in items) {
        val derived = newLegacyDropException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyLegacyDropException(item: LegacyDropException): String {
    return weightedLockerExceptionBucket(item.weightedLoad())
}
