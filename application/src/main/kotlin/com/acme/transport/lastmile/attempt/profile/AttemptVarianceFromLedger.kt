package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.lastmile.proofofdelivery.ledger.FallbackProofOfDeliveryLedger
import com.acme.transport.lastmile.proofofdelivery.ledger.classifyFallbackProofOfDeliveryLedger

fun summarizeFallbackProofOfDeliveryLedgersForVariance(items: MutableList<FallbackProofOfDeliveryLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAttemptVariancesFromLedger(items: MutableList<FallbackProofOfDeliveryLedger>): MutableList<AttemptVariance> {
    val out = mutableListOf<AttemptVariance>()
    for (item in items) {
        val derived = newAttemptVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planAttemptVariance(item: AttemptVariance): String {
    return classifyFallbackProofOfDeliveryLedger(item.measuredValue())
}
