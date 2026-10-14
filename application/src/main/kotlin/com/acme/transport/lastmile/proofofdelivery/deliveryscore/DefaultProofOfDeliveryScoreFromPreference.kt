package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.lastmile.redelivery.template.RedeliveryPreference
import com.acme.transport.lastmile.redelivery.template.classifyRedeliveryTemplate

fun summarizeRedeliveryPreferencesForScore(items: MutableList<RedeliveryPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDefaultProofOfDeliveryScoresFromPreference(items: MutableList<RedeliveryPreference>): MutableList<DefaultProofOfDeliveryScore> {
    val out = mutableListOf<DefaultProofOfDeliveryScore>()
    for (item in items) {
        val derived = newDefaultProofOfDeliveryScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefaultProofOfDeliveryScore(item: DefaultProofOfDeliveryScore): String {
    return classifyRedeliveryTemplate(item.netVolume())
}
