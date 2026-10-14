package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.dutyperiod.agreement.DutyPeriodAllocation
import com.acme.transport.crewing.dutyperiod.agreement.dutyPeriodAllocationWeight

fun summarizeDutyPeriodAllocationsForNotice(items: MutableList<DutyPeriodAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun derivePrimaryRestBreakNoticesFromAllocation(items: MutableList<DutyPeriodAllocation>): MutableList<PrimaryRestBreakNotice> {
    val out = mutableListOf<PrimaryRestBreakNotice>()
    for (item in items) {
        val derived = newPrimaryRestBreakNotice(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryRestBreakNotice(item: PrimaryRestBreakNotice): Double = dutyPeriodAllocationWeight(5, true)
