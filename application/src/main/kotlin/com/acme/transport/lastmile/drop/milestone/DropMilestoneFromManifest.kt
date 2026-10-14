package com.acme.transport.lastmile.drop.milestone

import com.acme.transport.lastmile.proofofdelivery.assignment.ProofOfDeliveryManifest
import com.acme.transport.lastmile.proofofdelivery.assignment.proofOfDeliveryAssignmentBucket

fun summarizeProofOfDeliveryManifestsForMilestone(items: MutableList<ProofOfDeliveryManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveDropMilestonesFromManifest(items: MutableList<ProofOfDeliveryManifest>): MutableList<DropMilestone> {
    val out = mutableListOf<DropMilestone>()
    for (item in items) {
        val derived = newDropMilestone(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectDropMilestone(item: DropMilestone): String = proofOfDeliveryAssignmentBucket(item.measuredValue())
