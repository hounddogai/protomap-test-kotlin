package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.terminal.sortation.bundle.GlobalSortationBundle
import com.acme.transport.terminal.sortation.bundle.primarySortationBatchWeight

fun summarizeGlobalSortationBundlesForAgreement(items: MutableList<GlobalSortationBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDwellAgreementsFromBundle(items: MutableList<GlobalSortationBundle>): MutableList<DwellAgreement> {
    val out = mutableListOf<DwellAgreement>()
    for (item in items) {
        val derived = newDwellAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeDwellAgreement(item: DwellAgreement): Double = primarySortationBatchWeight(8, true)
