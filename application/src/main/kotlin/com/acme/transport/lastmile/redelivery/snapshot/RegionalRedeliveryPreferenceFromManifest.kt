package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.BatchedProofOfDeliveryManifest
import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.batchedProofOfDeliveryManifestWeight

fun summarizeBatchedProofOfDeliveryManifestsForPreference(items: MutableList<BatchedProofOfDeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalRedeliveryPreferencesFromManifest(items: MutableList<BatchedProofOfDeliveryManifest>): MutableList<RegionalRedeliveryPreference> {
    val out = mutableListOf<RegionalRedeliveryPreference>()
    for (item in items) {
        val derived = newRegionalRedeliveryPreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileRegionalRedeliveryPreference(item: RegionalRedeliveryPreference): Double {
    return batchedProofOfDeliveryManifestWeight(6, true)
}
