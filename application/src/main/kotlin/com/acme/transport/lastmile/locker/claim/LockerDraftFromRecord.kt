package com.acme.transport.lastmile.locker.claim

import com.acme.transport.lastmile.attempt.manifest.StrictAttemptRecord
import com.acme.transport.lastmile.attempt.manifest.strictAttemptRecordBucket

fun summarizeStrictAttemptRecordsForDraft(items: MutableList<StrictAttemptRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLockerDraftsFromRecord(items: MutableList<StrictAttemptRecord>): MutableList<LockerDraft> {
    val out = mutableListOf<LockerDraft>()
    for (item in items) {
        val derived = newLockerDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignLockerDraft(item: LockerDraft): String = strictAttemptRecordBucket(item.measuredValue())
