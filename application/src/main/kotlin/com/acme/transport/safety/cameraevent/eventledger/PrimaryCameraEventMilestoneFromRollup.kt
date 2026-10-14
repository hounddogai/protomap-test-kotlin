package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.safety.violation.primaryviolationquota.DefaultViolationRollup
import com.acme.transport.safety.violation.primaryviolationquota.lenientViolationQuotaWeight

fun summarizeDefaultViolationRollupsForMilestone(items: MutableList<DefaultViolationRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun derivePrimaryCameraEventMilestonesFromRollup(items: MutableList<DefaultViolationRollup>): MutableList<PrimaryCameraEventMilestone> {
    val out = mutableListOf<PrimaryCameraEventMilestone>()
    for (item in items) {
        val derived = newPrimaryCameraEventMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scorePrimaryCameraEventMilestone(item: PrimaryCameraEventMilestone): Double {
    return lenientViolationQuotaWeight(8, true)
}
