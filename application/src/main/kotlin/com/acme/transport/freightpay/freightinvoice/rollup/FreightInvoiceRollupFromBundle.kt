package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.freightpay.audit.bundle.AuditBundle
import com.acme.transport.freightpay.audit.bundle.classifyAuditBundle
import com.acme.transport.freightpay.ratecard.bundle.RateCardBundle
import com.acme.transport.freightpay.ratecard.bundle.rateCardBundleBucket

fun summarizeAuditBundlesForRollup(items: MutableList<AuditBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFreightInvoiceRollupsFromBundle(items: MutableList<AuditBundle>): MutableList<FreightInvoiceRollup> {
    val out = mutableListOf<FreightInvoiceRollup>()
    for (item in items) {
        val derived = newFreightInvoiceRollup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeFreightInvoiceRollup(item: FreightInvoiceRollup): String = classifyAuditBundle(item.netVolume())

fun summarizeRateCardBundlesForRollup(items: MutableList<RateCardBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFreightInvoiceRollupsFromBundleExtra(items: MutableList<RateCardBundle>): MutableList<FreightInvoiceRollup> {
    val out = mutableListOf<FreightInvoiceRollup>()
    for (item in items) {
        val derived = newFreightInvoiceRollup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeFreightInvoiceRollupExtra(item: FreightInvoiceRollup): String {
    return rateCardBundleBucket(item.netVolume())
}
