package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.lastmile.attempt.ledger.LenientAttemptLedger
import com.acme.transport.lastmile.attempt.ledger.normalizeAttemptSnapshotCode

fun summarizeLenientAttemptLedgersForPreference(items: MutableList<LenientAttemptLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledProofOfDeliveryPreferencesFromLedger(items: MutableList<LenientAttemptLedger>): MutableList<ScheduledProofOfDeliveryPreference> {
    val out = mutableListOf<ScheduledProofOfDeliveryPreference>()
    for (item in items) {
        val derived = newScheduledProofOfDeliveryPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveScheduledProofOfDeliveryPreference(item: ScheduledProofOfDeliveryPreference): String {
    return normalizeAttemptSnapshotCode(item.code)
}
