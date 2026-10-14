package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.BatchedProofOfDeliveryManifest
import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.batchedProofOfDeliveryManifestWeight

fun summarizeBatchedProofOfDeliveryManifestsForClaim(items: MutableList<BatchedProofOfDeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalDropClaimsFromManifest(items: MutableList<BatchedProofOfDeliveryManifest>): MutableList<RegionalDropClaim> {
    val out = mutableListOf<RegionalDropClaim>()
    for (item in items) {
        val derived = newRegionalDropClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyRegionalDropClaimExtra(item: RegionalDropClaim): Double {
    return batchedProofOfDeliveryManifestWeight(8, true)
}
