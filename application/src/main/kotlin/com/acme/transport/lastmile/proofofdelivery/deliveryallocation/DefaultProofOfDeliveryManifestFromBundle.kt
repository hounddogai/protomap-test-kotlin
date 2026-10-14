package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.lastmile.proofofdelivery.allocation.ManualProofOfDeliveryBundle
import com.acme.transport.lastmile.proofofdelivery.allocation.classifySeasonalProofOfDeliveryCheckpoint

fun summarizeManualProofOfDeliveryBundlesForManifest(items: MutableList<ManualProofOfDeliveryBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDefaultProofOfDeliveryManifestsFromBundle(items: MutableList<ManualProofOfDeliveryBundle>): MutableList<DefaultProofOfDeliveryManifest> {
    val out = mutableListOf<DefaultProofOfDeliveryManifest>()
    for (item in items) {
        val derived = newDefaultProofOfDeliveryManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeDefaultProofOfDeliveryManifest(item: DefaultProofOfDeliveryManifest): String {
    return classifySeasonalProofOfDeliveryCheckpoint(item.weightedVolume())
}
