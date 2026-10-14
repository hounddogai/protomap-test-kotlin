package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.lastmile.locker.assignment.LockerCycle
import com.acme.transport.lastmile.locker.assignment.lockerCycleWeight

fun summarizeLockerCyclesForRollup(items: MutableList<LockerCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveBatchedProofOfDeliveryRollupsFromCycle(items: MutableList<LockerCycle>): MutableList<BatchedProofOfDeliveryRollup> {
    val out = mutableListOf<BatchedProofOfDeliveryRollup>()
    for (item in items) {
        val derived = newBatchedProofOfDeliveryRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignBatchedProofOfDeliveryRollup(item: BatchedProofOfDeliveryRollup): Double = lockerCycleWeight(6, true)
