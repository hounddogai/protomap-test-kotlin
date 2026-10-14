package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.qualification.rule.RollingQualificationRule
import com.acme.transport.crewing.qualification.rule.qualificationBatchBucket

fun summarizeRollingQualificationRulesForException(items: MutableList<RollingQualificationRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveScheduledDutyPeriodExceptionsFromRule(items: MutableList<RollingQualificationRule>): MutableList<ScheduledDutyPeriodException> {
    val out = mutableListOf<ScheduledDutyPeriodException>()
    for (item in items) {
        val derived = newScheduledDutyPeriodException(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateScheduledDutyPeriodException(item: ScheduledDutyPeriodException): String {
    return qualificationBatchBucket(item.effectiveLoad())
}
