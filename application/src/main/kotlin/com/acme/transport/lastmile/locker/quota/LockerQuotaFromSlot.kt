package com.acme.transport.lastmile.locker.quota

import com.acme.transport.lastmile.attempt.attemptdigest.AttemptSlot
import com.acme.transport.lastmile.attempt.attemptdigest.attemptSlotBucket

fun summarizeAttemptSlotsForQuota(items: MutableList<AttemptSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLockerQuotasFromSlot(items: MutableList<AttemptSlot>): MutableList<LockerQuota> {
    val out = mutableListOf<LockerQuota>()
    for (item in items) {
        val derived = newLockerQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLockerQuota(item: LockerQuota): String = attemptSlotBucket(item.projectedLoad())
