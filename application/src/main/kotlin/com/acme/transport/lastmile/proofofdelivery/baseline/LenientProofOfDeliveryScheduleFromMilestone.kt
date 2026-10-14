package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.DefaultProofOfDeliveryMilestone
import com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest.normalizeBatchedProofOfDeliveryManifestCode

fun summarizeDefaultProofOfDeliveryMilestonesForSchedule(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientProofOfDeliverySchedulesFromMilestone(items: MutableList<DefaultProofOfDeliveryMilestone>): MutableList<LenientProofOfDeliverySchedule> {
    val out = mutableListOf<LenientProofOfDeliverySchedule>()
    for (item in items) {
        val derived = newLenientProofOfDeliverySchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeLenientProofOfDeliverySchedule(item: LenientProofOfDeliverySchedule): String {
    return normalizeBatchedProofOfDeliveryManifestCode(item.code)
}
