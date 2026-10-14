package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.lastmile.attempt.attemptdraft.GlobalAttemptDraft
import com.acme.transport.lastmile.attempt.attemptdraft.classifyDefaultAttemptCheckpoint

fun summarizeGlobalAttemptDraftsForRecord(items: MutableList<GlobalAttemptDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveAdaptiveProofOfDeliveryRecordsFromDraft(items: MutableList<GlobalAttemptDraft>): MutableList<AdaptiveProofOfDeliveryRecord> {
    val out = mutableListOf<AdaptiveProofOfDeliveryRecord>()
    for (item in items) {
        val derived = newAdaptiveProofOfDeliveryRecord(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeAdaptiveProofOfDeliveryRecord(item: AdaptiveProofOfDeliveryRecord): String {
    return classifyDefaultAttemptCheckpoint(item.netVolume())
}
