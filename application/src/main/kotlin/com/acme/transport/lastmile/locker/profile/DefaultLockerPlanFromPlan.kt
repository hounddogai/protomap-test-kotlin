package com.acme.transport.lastmile.locker.profile

import com.acme.transport.lastmile.attempt.plan.LenientAttemptPlan
import com.acme.transport.lastmile.attempt.plan.lenientAttemptPlanBucket

fun summarizeLenientAttemptPlansForPlan(items: MutableList<LenientAttemptPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveDefaultLockerPlansFromPlan(items: MutableList<LenientAttemptPlan>): MutableList<DefaultLockerPlan> {
    val out = mutableListOf<DefaultLockerPlan>()
    for (item in items) {
        val derived = newDefaultLockerPlan(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignDefaultLockerPlan(item: DefaultLockerPlan): String = lenientAttemptPlanBucket(item.effectiveVolume())
