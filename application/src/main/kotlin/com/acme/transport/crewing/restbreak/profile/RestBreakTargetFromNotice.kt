package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.dutyperiod.notice.DutyPeriodNotice
import com.acme.transport.crewing.dutyperiod.notice.dutyPeriodScheduleBucket

fun summarizeDutyPeriodNoticesForTarget(items: MutableList<DutyPeriodNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveRestBreakTargetsFromNotice(items: MutableList<DutyPeriodNotice>): MutableList<RestBreakTarget> {
    val out = mutableListOf<RestBreakTarget>()
    for (item in items) {
        val derived = newRestBreakTarget(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveRestBreakTarget(item: RestBreakTarget): String = dutyPeriodScheduleBucket(item.adjustedExposure())
