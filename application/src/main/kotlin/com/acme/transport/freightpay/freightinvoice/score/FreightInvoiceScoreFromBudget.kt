package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.freightpay.accessorial.manifest.AccessorialBudget
import com.acme.transport.freightpay.accessorial.manifest.classifyAccessorialManifest

fun summarizeAccessorialBudgetsForScore(items: MutableList<AccessorialBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveFreightInvoiceScoresFromBudget(items: MutableList<AccessorialBudget>): MutableList<FreightInvoiceScore> {
    val out = mutableListOf<FreightInvoiceScore>()
    for (item in items) {
        val derived = newFreightInvoiceScore(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFreightInvoiceScore(item: FreightInvoiceScore): String {
    return classifyAccessorialManifest(item.adjustedExposure())
}
