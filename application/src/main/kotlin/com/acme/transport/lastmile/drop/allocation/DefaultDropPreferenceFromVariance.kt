package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.lastmile.proofofdelivery.score.ProofOfDeliveryVariance
import com.acme.transport.lastmile.proofofdelivery.score.classifyProofOfDeliveryVariance

fun summarizeProofOfDeliveryVariancesForPreference(items: MutableList<ProofOfDeliveryVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDefaultDropPreferencesFromVariance(items: MutableList<ProofOfDeliveryVariance>): MutableList<DefaultDropPreference> {
    val out = mutableListOf<DefaultDropPreference>()
    for (item in items) {
        val derived = newDefaultDropPreference(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultDropPreference(item: DefaultDropPreference): String {
    return classifyProofOfDeliveryVariance(item.netExposure())
}
