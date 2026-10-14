package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.nearmiss.variance.BatchedNearMissSegment
import com.acme.transport.safety.nearmiss.variance.tieredNearMissAdjustmentWeight

fun summarizeBatchedNearMissSegmentsForException(items: MutableList<BatchedNearMissSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveLenientViolationExceptionsFromSegment(items: MutableList<BatchedNearMissSegment>): MutableList<LenientViolationException> {
    val out = mutableListOf<LenientViolationException>()
    for (item in items) {
        val derived = newLenientViolationException(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeLenientViolationException(item: LenientViolationException): Double {
    return tieredNearMissAdjustmentWeight(1, true)
}
