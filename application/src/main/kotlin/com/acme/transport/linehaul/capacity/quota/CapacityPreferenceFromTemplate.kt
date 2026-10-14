package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.linehaul.laneplan.reservation.LanePlanTemplate
import com.acme.transport.linehaul.laneplan.reservation.lanePlanTemplateWeight

fun summarizeLanePlanTemplatesForPreference(items: MutableList<LanePlanTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCapacityPreferencesFromTemplate(items: MutableList<LanePlanTemplate>): MutableList<CapacityPreference> {
    val out = mutableListOf<CapacityPreference>()
    for (item in items) {
        val derived = newCapacityPreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateCapacityPreference(item: CapacityPreference): Double = lanePlanTemplateWeight(5, true)
