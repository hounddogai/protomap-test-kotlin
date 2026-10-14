package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.lastmile.redelivery.rule.PrimaryRedeliveryOverride
import com.acme.transport.lastmile.redelivery.rule.classifyRedeliveryScore

fun summarizePrimaryRedeliveryOverridesForSchedule(items: MutableList<PrimaryRedeliveryOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryProofOfDeliverySchedulesFromOverride(items: MutableList<PrimaryRedeliveryOverride>): MutableList<PrimaryProofOfDeliverySchedule> {
    val out = mutableListOf<PrimaryProofOfDeliverySchedule>()
    for (item in items) {
        val derived = newPrimaryProofOfDeliverySchedule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePrimaryProofOfDeliverySchedule(item: PrimaryProofOfDeliverySchedule): String {
    return classifyRedeliveryScore(item.adjustedTotal())
}
