package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.defect.agreement.ManualDefectAgreement
import com.acme.transport.fleetops.defect.agreement.defectBatchWeight

fun summarizeManualDefectAgreementsForSlot(items: MutableList<ManualDefectAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTireSlotsFromAgreement(items: MutableList<ManualDefectAgreement>): MutableList<TireSlot> {
    val out = mutableListOf<TireSlot>()
    for (item in items) {
        val derived = newTireSlot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeTireSlot(item: TireSlot): Double = defectBatchWeight(5, true)
