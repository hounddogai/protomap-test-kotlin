package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.lastmile.attempt.overrideitem.BatchedAttemptOverride
import com.acme.transport.lastmile.attempt.overrideitem.classifyRegionalAttemptRule

fun summarizeBatchedAttemptOverridesForAgreement(items: MutableList<BatchedAttemptOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLegacyLockerAgreementsFromOverride(items: MutableList<BatchedAttemptOverride>): MutableList<LegacyLockerAgreement> {
    val out = mutableListOf<LegacyLockerAgreement>()
    for (item in items) {
        val derived = newLegacyLockerAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreLegacyLockerAgreement(item: LegacyLockerAgreement): String {
    return classifyRegionalAttemptRule(item.measuredValue())
}
