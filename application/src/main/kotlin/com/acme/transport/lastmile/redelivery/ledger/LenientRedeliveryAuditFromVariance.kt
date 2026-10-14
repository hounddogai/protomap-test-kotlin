package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.lastmile.attempt.profile.AttemptVariance
import com.acme.transport.lastmile.attempt.profile.attemptVarianceBucket

fun summarizeAttemptVariancesForAudit(items: MutableList<AttemptVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientRedeliveryAuditsFromVariance(items: MutableList<AttemptVariance>): MutableList<LenientRedeliveryAudit> {
    val out = mutableListOf<LenientRedeliveryAudit>()
    for (item in items) {
        val derived = newLenientRedeliveryAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLenientRedeliveryAudit(item: LenientRedeliveryAudit): String {
    return attemptVarianceBucket(item.projectedTotal())
}
