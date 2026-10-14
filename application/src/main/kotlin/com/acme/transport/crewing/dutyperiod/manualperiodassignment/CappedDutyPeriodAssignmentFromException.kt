package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.crewing.restbreak.claim.TieredRestBreakException
import com.acme.transport.crewing.restbreak.claim.normalizeTieredRestBreakExceptionCode

fun summarizeTieredRestBreakExceptionsForAssignment(items: MutableList<TieredRestBreakException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveCappedDutyPeriodAssignmentsFromException(items: MutableList<TieredRestBreakException>): MutableList<CappedDutyPeriodAssignment> {
    val out = mutableListOf<CappedDutyPeriodAssignment>()
    for (item in items) {
        val derived = newCappedDutyPeriodAssignment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveCappedDutyPeriodAssignment(item: CappedDutyPeriodAssignment): String {
    return normalizeTieredRestBreakExceptionCode(item.code)
}
