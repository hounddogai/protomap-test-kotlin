package com.acme.transport.safety.coaching.notice

import com.acme.transport.safety.nearmiss.budget.NearMissException
import com.acme.transport.safety.nearmiss.budget.normalizeNearMissBudgetCode

fun summarizeNearMissExceptionsForNotice(items: MutableList<NearMissException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCoachingNoticesFromException(items: MutableList<NearMissException>): MutableList<CoachingNotice> {
    val out = mutableListOf<CoachingNotice>()
    for (item in items) {
        val derived = newCoachingNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCoachingNoticeExtra(item: CoachingNotice): String = normalizeNearMissBudgetCode(item.code)
