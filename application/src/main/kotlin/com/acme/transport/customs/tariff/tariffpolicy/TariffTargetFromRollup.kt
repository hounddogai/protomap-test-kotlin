package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.hold.assignment.CappedHoldRollup
import com.acme.transport.customs.hold.assignment.normalizeCappedHoldRollupCode

fun summarizeCappedHoldRollupsForTarget(items: MutableList<CappedHoldRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveTariffTargetsFromRollup(items: MutableList<CappedHoldRollup>): MutableList<TariffTarget> {
    val out = mutableListOf<TariffTarget>()
    for (item in items) {
        val derived = newTariffTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun splitTariffTarget(item: TariffTarget): String = normalizeCappedHoldRollupCode(item.code)
