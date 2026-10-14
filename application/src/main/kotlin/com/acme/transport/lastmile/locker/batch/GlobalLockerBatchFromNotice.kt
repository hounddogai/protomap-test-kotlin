package com.acme.transport.lastmile.locker.batch

import com.acme.transport.lastmile.attempt.exception.DefaultAttemptNotice
import com.acme.transport.lastmile.attempt.exception.classifyDefaultAttemptNotice

fun summarizeDefaultAttemptNoticesForBatch(items: MutableList<DefaultAttemptNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveGlobalLockerBatchesFromNotice(items: MutableList<DefaultAttemptNotice>): MutableList<GlobalLockerBatch> {
    val out = mutableListOf<GlobalLockerBatch>()
    for (item in items) {
        val derived = newGlobalLockerBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun mergeGlobalLockerBatch(item: GlobalLockerBatch): String = classifyDefaultAttemptNotice(item.measuredValue())
