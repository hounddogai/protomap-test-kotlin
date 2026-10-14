package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.clearance.target.ClearanceException
import com.acme.transport.customs.clearance.target.classifyPrimaryClearanceGroup

fun summarizeClearanceExceptionsForSchedule(items: MutableList<ClearanceException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveTariffSchedulesFromException(items: MutableList<ClearanceException>): MutableList<TariffSchedule> {
    val out = mutableListOf<TariffSchedule>()
    for (item in items) {
        val derived = newTariffSchedule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeTariffSchedule(item: TariffSchedule): String = classifyPrimaryClearanceGroup(item.adjustedLoad())
