package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.customs.broker.budget.BrokerSlot
import com.acme.transport.customs.broker.budget.classifyBrokerRequest

fun summarizeBrokerSlotsForException(items: MutableList<BrokerSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveTariffExceptionsFromSlot(items: MutableList<BrokerSlot>): MutableList<TariffException> {
    val out = mutableListOf<TariffException>()
    for (item in items) {
        val derived = newTariffException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeTariffException(item: TariffException): String = classifyBrokerRequest(item.effectiveLoad())
