package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.lastmile.proofofdelivery.claim.LegacyProofOfDeliveryRequest
import com.acme.transport.lastmile.proofofdelivery.claim.classifyProofOfDeliveryClaim

fun summarizeLegacyProofOfDeliveryRequestsForClaim(items: MutableList<LegacyProofOfDeliveryRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveBatchedLockerClaimsFromRequest(items: MutableList<LegacyProofOfDeliveryRequest>): MutableList<BatchedLockerClaim> {
    val out = mutableListOf<BatchedLockerClaim>()
    for (item in items) {
        val derived = newBatchedLockerClaim(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankBatchedLockerClaim(item: BatchedLockerClaim): String = classifyProofOfDeliveryClaim(item.measuredValue())
