package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.lastmile.locker.schedule.LockerAdjustment
import com.acme.transport.lastmile.locker.schedule.lockerOverrideBucket

fun summarizeLockerAdjustmentsForThreshold(items: MutableList<LockerAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveRedeliveryThresholdsFromAdjustment(items: MutableList<LockerAdjustment>): MutableList<RedeliveryThreshold> {
    val out = mutableListOf<RedeliveryThreshold>()
    for (item in items) {
        val derived = newRedeliveryThreshold(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRedeliveryThreshold(item: RedeliveryThreshold): String = lockerOverrideBucket(item.adjustedLoad())
