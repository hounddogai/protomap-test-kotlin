package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.lastmile.attempt.template.AttemptCycle
import com.acme.transport.lastmile.attempt.template.cappedAttemptExceptionWeight

fun summarizeAttemptCyclesForTarget(items: MutableList<AttemptCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveProofOfDeliveryTargetsFromCycle(items: MutableList<AttemptCycle>): MutableList<ProofOfDeliveryTarget> {
    val out = mutableListOf<ProofOfDeliveryTarget>()
    for (item in items) {
        val derived = newProofOfDeliveryTarget(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankProofOfDeliveryTarget(item: ProofOfDeliveryTarget): Double = cappedAttemptExceptionWeight(1, true)
