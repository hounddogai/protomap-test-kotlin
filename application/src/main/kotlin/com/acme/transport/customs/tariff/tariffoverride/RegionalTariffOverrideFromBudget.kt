package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.customs.inspection.rule.InspectionBudget
import com.acme.transport.customs.inspection.rule.inspectionRuleBucket

fun summarizeInspectionBudgetsForOverride(items: MutableList<InspectionBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalTariffOverridesFromBudget(items: MutableList<InspectionBudget>): MutableList<RegionalTariffOverride> {
    val out = mutableListOf<RegionalTariffOverride>()
    for (item in items) {
        val derived = newRegionalTariffOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignRegionalTariffOverride(item: RegionalTariffOverride): String {
    return inspectionRuleBucket(item.measuredValue())
}
