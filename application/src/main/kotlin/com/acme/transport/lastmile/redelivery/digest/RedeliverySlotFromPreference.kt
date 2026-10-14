package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.lastmile.locker.preference.LockerPreference
import com.acme.transport.lastmile.locker.preference.lockerExceptionBucket

fun summarizeLockerPreferencesForSlot(items: MutableList<LockerPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRedeliverySlotsFromPreference(items: MutableList<LockerPreference>): MutableList<RedeliverySlot> {
    val out = mutableListOf<RedeliverySlot>()
    for (item in items) {
        val derived = newRedeliverySlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRedeliverySlot(item: RedeliverySlot): String = lockerExceptionBucket(item.effectiveTotal())
