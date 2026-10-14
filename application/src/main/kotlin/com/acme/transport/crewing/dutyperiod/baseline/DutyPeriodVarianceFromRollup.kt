package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.dutyperiod.target.DefaultDutyPeriodRollup
import com.acme.transport.crewing.dutyperiod.target.lenientDutyPeriodBundleBucket

fun summarizeDefaultDutyPeriodRollupsForVariance(items: MutableList<DefaultDutyPeriodRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDutyPeriodVariancesFromRollup(items: MutableList<DefaultDutyPeriodRollup>): MutableList<DutyPeriodVariance> {
    val out = mutableListOf<DutyPeriodVariance>()
    for (item in items) {
        val derived = newDutyPeriodVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileDutyPeriodVariance(item: DutyPeriodVariance): String {
    return lenientDutyPeriodBundleBucket(item.measuredValue())
}
