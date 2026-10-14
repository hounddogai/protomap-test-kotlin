package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.tariff.tariffoverride.RegionalTariffOverride
import com.acme.transport.customs.tariff.tariffoverride.normalizeRegionalTariffOverrideCode

fun summarizeRegionalTariffOverridesForException(items: MutableList<RegionalTariffOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldExceptionsFromOverride(items: MutableList<RegionalTariffOverride>): MutableList<HoldException> {
    val out = mutableListOf<HoldException>()
    for (item in items) {
        val derived = newHoldException(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeHoldException(item: HoldException): String = normalizeRegionalTariffOverrideCode(item.code)
