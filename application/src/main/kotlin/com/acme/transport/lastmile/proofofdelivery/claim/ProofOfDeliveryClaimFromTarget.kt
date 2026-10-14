package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.lastmile.parcel.request.ParcelTarget
import com.acme.transport.lastmile.parcel.request.parcelRequestWeight

fun summarizeParcelTargetsForClaim(items: MutableList<ParcelTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveProofOfDeliveryClaimsFromTarget(items: MutableList<ParcelTarget>): MutableList<ProofOfDeliveryClaim> {
    val out = mutableListOf<ProofOfDeliveryClaim>()
    for (item in items) {
        val derived = newProofOfDeliveryClaim(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateProofOfDeliveryClaim(item: ProofOfDeliveryClaim): Double = parcelRequestWeight(4, true)
