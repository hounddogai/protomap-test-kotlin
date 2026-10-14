package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.idling.notice.IdlingNotice
import com.acme.transport.emissions.idling.notice.normalizeIdlingSnapshotCode

fun summarizeIdlingNoticesForException(items: MutableList<IdlingNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveIdlingExceptionsFromNotice(items: MutableList<IdlingNotice>): MutableList<IdlingException> {
    val out = mutableListOf<IdlingException>()
    for (item in items) {
        val derived = newIdlingException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyIdlingExceptionExtra(item: IdlingException): String = normalizeIdlingSnapshotCode(item.code)
