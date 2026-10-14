package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.accessorial.draft.AccessorialDraft
import com.acme.transport.freightpay.accessorial.draft.classifyAccessorialSchedule

fun summarizeAccessorialDraftsForAssignment(items: MutableList<AccessorialDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFuelSurchargeAssignmentsFromDraft(items: MutableList<AccessorialDraft>): MutableList<FuelSurchargeAssignment> {
    val out = mutableListOf<FuelSurchargeAssignment>()
    for (item in items) {
        val derived = newFuelSurchargeAssignment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFuelSurchargeAssignment(item: FuelSurchargeAssignment): String {
    return classifyAccessorialSchedule(item.netExposure())
}
