package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.lastmile.proofofdelivery.review.CappedProofOfDeliveryProfile
import com.acme.transport.lastmile.proofofdelivery.review.normalizeProofOfDeliveryCycleCode

fun summarizeCappedProofOfDeliveryProfilesForPlan(items: MutableList<CappedProofOfDeliveryProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientAttemptPlansFromProfile(items: MutableList<CappedProofOfDeliveryProfile>): MutableList<LenientAttemptPlan> {
    val out = mutableListOf<LenientAttemptPlan>()
    for (item in items) {
        val derived = newLenientAttemptPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientAttemptPlan(item: LenientAttemptPlan): String = normalizeProofOfDeliveryCycleCode(item.code)
