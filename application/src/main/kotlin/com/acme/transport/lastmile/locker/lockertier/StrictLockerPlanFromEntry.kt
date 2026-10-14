package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.lastmile.proofofdelivery.deliveryclaim.RollingProofOfDeliveryEntry
import com.acme.transport.lastmile.proofofdelivery.deliveryclaim.manualProofOfDeliveryClaimBucket

fun summarizeRollingProofOfDeliveryEntriesForPlan(items: MutableList<RollingProofOfDeliveryEntry>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveStrictLockerPlansFromEntry(items: MutableList<RollingProofOfDeliveryEntry>): MutableList<StrictLockerPlan> {
    val out = mutableListOf<StrictLockerPlan>()
    for (item in items) {
        val derived = newStrictLockerPlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun matchStrictLockerPlan(item: StrictLockerPlan): String {
    return manualProofOfDeliveryClaimBucket(item.effectiveVolume())
}
