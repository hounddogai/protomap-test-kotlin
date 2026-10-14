package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.lastmile.attempt.allocation.AttemptAdjustment
import com.acme.transport.lastmile.attempt.allocation.attemptAdjustmentBucket

fun summarizeAttemptAdjustmentsForAgreement(items: MutableList<AttemptAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRedeliveryAgreementsFromAdjustment(items: MutableList<AttemptAdjustment>): MutableList<RedeliveryAgreement> {
    val out = mutableListOf<RedeliveryAgreement>()
    for (item in items) {
        val derived = newRedeliveryAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRedeliveryAgreement(item: RedeliveryAgreement): String = attemptAdjustmentBucket(item.effectiveTotal())
