package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.crewing.dutyperiod.assignment.DutyPeriodRollup
import com.acme.transport.crewing.dutyperiod.assignment.dutyPeriodRollupWeight

fun summarizeDutyPeriodRollupsForException(items: MutableList<DutyPeriodRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveQualificationExceptionsFromRollup(items: MutableList<DutyPeriodRollup>): MutableList<QualificationException> {
    val out = mutableListOf<QualificationException>()
    for (item in items) {
        val derived = newQualificationException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreQualificationException(item: QualificationException): Double = dutyPeriodRollupWeight(3, true)
