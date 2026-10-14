package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.lastmile.proofofdelivery.assignment.ProofOfDeliveryAssignment
import com.acme.transport.lastmile.proofofdelivery.assignment.proofOfDeliveryScheduleBucket

fun summarizeProofOfDeliveryAssignmentsForManifest(items: MutableList<ProofOfDeliveryAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLockerManifestsFromAssignment(items: MutableList<ProofOfDeliveryAssignment>): MutableList<LockerManifest> {
    val out = mutableListOf<LockerManifest>()
    for (item in items) {
        val derived = newLockerManifest(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLockerManifest(item: LockerManifest): String = proofOfDeliveryScheduleBucket(item.projectedTotal())
