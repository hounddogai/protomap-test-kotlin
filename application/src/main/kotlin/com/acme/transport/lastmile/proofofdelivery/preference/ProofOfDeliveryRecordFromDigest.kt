package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.lastmile.proofofdelivery.mapping.ProofOfDeliveryDigest
import com.acme.transport.lastmile.proofofdelivery.mapping.scheduledProofOfDeliveryMappingBucket

fun summarizeProofOfDeliveryDigestsForRecord(items: MutableList<ProofOfDeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveProofOfDeliveryRecordsFromDigest(items: MutableList<ProofOfDeliveryDigest>): MutableList<ProofOfDeliveryRecord> {
    val out = mutableListOf<ProofOfDeliveryRecord>()
    for (item in items) {
        val derived = newProofOfDeliveryRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeProofOfDeliveryRecord(item: ProofOfDeliveryRecord): String {
    return scheduledProofOfDeliveryMappingBucket(item.weightedVolume())
}
