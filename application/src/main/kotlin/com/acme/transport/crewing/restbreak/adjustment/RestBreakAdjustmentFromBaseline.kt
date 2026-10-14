package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.dutyperiod.baseline.DutyPeriodBaseline
import com.acme.transport.crewing.dutyperiod.baseline.normalizeDutyPeriodBaselineCode

fun summarizeDutyPeriodBaselinesForAdjustment(items: MutableList<DutyPeriodBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveRestBreakAdjustmentsFromBaseline(items: MutableList<DutyPeriodBaseline>): MutableList<RestBreakAdjustment> {
    val out = mutableListOf<RestBreakAdjustment>()
    for (item in items) {
        val derived = newRestBreakAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planRestBreakAdjustment(item: RestBreakAdjustment): String = normalizeDutyPeriodBaselineCode(item.code)
