package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.emissions.emissionfactor.factorpreference.PrimaryEmissionFactorReview
import com.acme.transport.emissions.emissionfactor.factorpreference.batchedEmissionFactorPolicyBucket

fun summarizePrimaryEmissionFactorReviewsForDigest(items: MutableList<PrimaryEmissionFactorReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryEvChargeDigestsFromReview(items: MutableList<PrimaryEmissionFactorReview>): MutableList<PrimaryEvChargeDigest> {
    val out = mutableListOf<PrimaryEvChargeDigest>()
    for (item in items) {
        val derived = newPrimaryEvChargeDigest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectPrimaryEvChargeDigest(item: PrimaryEvChargeDigest): String {
    return batchedEmissionFactorPolicyBucket(item.weightedLoad())
}
