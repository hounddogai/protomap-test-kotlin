package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.lastmile.locker.tier.TieredLockerAgreement
import com.acme.transport.lastmile.locker.tier.lockerTierBucket

fun summarizeTieredLockerAgreementsForRequest(items: MutableList<TieredLockerAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLegacyProofOfDeliveryRequestsFromAgreement(items: MutableList<TieredLockerAgreement>): MutableList<LegacyProofOfDeliveryRequest> {
    val out = mutableListOf<LegacyProofOfDeliveryRequest>()
    for (item in items) {
        val derived = newLegacyProofOfDeliveryRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLegacyProofOfDeliveryRequest(item: LegacyProofOfDeliveryRequest): String {
    return lockerTierBucket(item.adjustedExposure())
}
