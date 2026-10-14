package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.lastmile.attempt.milestone.FallbackAttemptMilestone
import com.acme.transport.lastmile.attempt.milestone.normalizeFallbackAttemptMilestoneCode
import com.acme.transport.lastmile.redelivery.milestone.RedeliveryMilestone
import com.acme.transport.lastmile.redelivery.milestone.redeliveryMilestoneWeight

fun summarizeFallbackAttemptMilestonesForSegment(items: MutableList<FallbackAttemptMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveProofOfDeliverySegmentsFromMilestone(items: MutableList<FallbackAttemptMilestone>): MutableList<ProofOfDeliverySegment> {
    val out = mutableListOf<ProofOfDeliverySegment>()
    for (item in items) {
        val derived = newProofOfDeliverySegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeProofOfDeliverySegment(item: ProofOfDeliverySegment): String {
    return normalizeFallbackAttemptMilestoneCode(item.code)
}

fun summarizeRedeliveryMilestonesForSegment(items: MutableList<RedeliveryMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveProofOfDeliverySegmentsFromMilestoneExtra(items: MutableList<RedeliveryMilestone>): MutableList<ProofOfDeliverySegment> {
    val out = mutableListOf<ProofOfDeliverySegment>()
    for (item in items) {
        val derived = newProofOfDeliverySegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeProofOfDeliverySegment(item: ProofOfDeliverySegment): Double = redeliveryMilestoneWeight(7, true)
