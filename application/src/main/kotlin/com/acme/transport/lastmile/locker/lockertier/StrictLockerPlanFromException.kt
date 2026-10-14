package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.lastmile.attempt.adaptiveattemptrequest.FallbackAttemptException
import com.acme.transport.lastmile.attempt.adaptiveattemptrequest.classifyFallbackAttemptException

fun summarizeFallbackAttemptExceptionsForPlan(items: MutableList<FallbackAttemptException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictLockerPlansFromException(items: MutableList<FallbackAttemptException>): MutableList<StrictLockerPlan> {
    val out = mutableListOf<StrictLockerPlan>()
    for (item in items) {
        val derived = newStrictLockerPlan(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveStrictLockerPlan(item: StrictLockerPlan): String {
    return classifyFallbackAttemptException(item.effectiveVolume())
}
