package com.acme.transport.customs.broker.audit

import com.acme.transport.customs.tariff.cycle.TariffAudit
import com.acme.transport.customs.tariff.cycle.tariffCycleWeight

fun summarizeTariffAuditsForOverride(items: MutableList<TariffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveManualBrokerOverridesFromAudit(items: MutableList<TariffAudit>): MutableList<ManualBrokerOverride> {
    val out = mutableListOf<ManualBrokerOverride>()
    for (item in items) {
        val derived = newManualBrokerOverride(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateManualBrokerOverride(item: ManualBrokerOverride): Double = tariffCycleWeight(9, true)
