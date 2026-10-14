package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.lastmile.proofofdelivery.audit.ProofOfDeliveryAudit
import com.acme.transport.lastmile.proofofdelivery.audit.proofOfDeliveryOverrideBucket

fun summarizeProofOfDeliveryAuditsForSegment(items: MutableList<ProofOfDeliveryAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveAttemptSegmentsFromAudit(items: MutableList<ProofOfDeliveryAudit>): MutableList<AttemptSegment> {
    val out = mutableListOf<AttemptSegment>()
    for (item in items) {
        val derived = newAttemptSegment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceAttemptSegment(item: AttemptSegment): String = proofOfDeliveryOverrideBucket(item.adjustedLoad())
