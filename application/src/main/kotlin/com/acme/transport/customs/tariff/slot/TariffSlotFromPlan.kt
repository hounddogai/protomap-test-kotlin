package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.inspection.inspectionplan.LenientInspectionPlan
import com.acme.transport.customs.inspection.inspectionplan.normalizeLenientInspectionPlanCode

fun summarizeLenientInspectionPlansForSlot(items: MutableList<LenientInspectionPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTariffSlotsFromPlan(items: MutableList<LenientInspectionPlan>): MutableList<TariffSlot> {
    val out = mutableListOf<TariffSlot>()
    for (item in items) {
        val derived = newTariffSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun matchTariffSlot(item: TariffSlot): String = normalizeLenientInspectionPlanCode(item.code)
