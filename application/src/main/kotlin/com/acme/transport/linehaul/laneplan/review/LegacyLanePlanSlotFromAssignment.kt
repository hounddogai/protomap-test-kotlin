package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.linehaul.trailer.budget.RollingTrailerAssignment
import com.acme.transport.linehaul.trailer.budget.adaptiveTrailerRequestBucket

fun summarizeRollingTrailerAssignmentsForSlot(items: MutableList<RollingTrailerAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLegacyLanePlanSlotsFromAssignment(items: MutableList<RollingTrailerAssignment>): MutableList<LegacyLanePlanSlot> {
    val out = mutableListOf<LegacyLanePlanSlot>()
    for (item in items) {
        val derived = newLegacyLanePlanSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planLegacyLanePlanSlot(item: LegacyLanePlanSlot): String = adaptiveTrailerRequestBucket(item.adjustedTotal())
