package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.lastmile.locker.slot.LockerMetric
import com.acme.transport.lastmile.locker.slot.normalizeLockerSlotCode

fun summarizeLockerMetricsForMetric(items: MutableList<LockerMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveProofOfDeliveryMetricsFromMetric(items: MutableList<LockerMetric>): MutableList<ProofOfDeliveryMetric> {
    val out = mutableListOf<ProofOfDeliveryMetric>()
    for (item in items) {
        val derived = newProofOfDeliveryMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleProofOfDeliveryMetric(item: ProofOfDeliveryMetric): String = normalizeLockerSlotCode(item.code)
