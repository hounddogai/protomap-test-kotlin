package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.inspection.adjustment.InspectionBaseline
import com.acme.transport.customs.inspection.adjustment.inspectionRollupWeight

fun summarizeInspectionBaselinesForLedger(items: MutableList<InspectionBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldLedgersFromBaseline(items: MutableList<InspectionBaseline>): MutableList<HoldLedger> {
    val out = mutableListOf<HoldLedger>()
    for (item in items) {
        val derived = newHoldLedger(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateHoldLedger(item: HoldLedger): Double = inspectionRollupWeight(4, true)
