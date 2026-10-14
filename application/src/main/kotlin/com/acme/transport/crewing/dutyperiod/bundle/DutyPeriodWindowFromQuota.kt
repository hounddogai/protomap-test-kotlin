package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.qualification.rollup.PrimaryQualificationQuota
import com.acme.transport.crewing.qualification.rollup.primaryQualificationQuotaWeight

fun summarizePrimaryQualificationQuotasForWindow(items: MutableList<PrimaryQualificationQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveDutyPeriodWindowsFromQuota(items: MutableList<PrimaryQualificationQuota>): MutableList<DutyPeriodWindow> {
    val out = mutableListOf<DutyPeriodWindow>()
    for (item in items) {
        val derived = newDutyPeriodWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDutyPeriodWindow(item: DutyPeriodWindow): Double = primaryQualificationQuotaWeight(7, true)
