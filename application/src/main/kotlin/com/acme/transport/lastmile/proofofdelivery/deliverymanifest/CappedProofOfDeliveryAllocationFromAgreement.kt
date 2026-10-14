package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.parcel.exception.ParcelAgreement
import com.acme.transport.lastmile.parcel.exception.classifyRegionalParcelReview

fun summarizeParcelAgreementsForAllocation(items: MutableList<ParcelAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveCappedProofOfDeliveryAllocationsFromAgreement(items: MutableList<ParcelAgreement>): MutableList<CappedProofOfDeliveryAllocation> {
    val out = mutableListOf<CappedProofOfDeliveryAllocation>()
    for (item in items) {
        val derived = newCappedProofOfDeliveryAllocation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignCappedProofOfDeliveryAllocation(item: CappedProofOfDeliveryAllocation): String {
    return classifyRegionalParcelReview(item.effectiveLoad())
}
