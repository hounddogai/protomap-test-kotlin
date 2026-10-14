package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.freightpay.freightinvoice.template.FallbackFreightInvoiceNotice
import com.acme.transport.freightpay.freightinvoice.template.primaryFreightInvoiceTemplateWeight

fun summarizeFallbackFreightInvoiceNoticesForAdjustment(items: MutableList<FallbackFreightInvoiceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFreightInvoiceAdjustmentsFromNotice(items: MutableList<FallbackFreightInvoiceNotice>): MutableList<FreightInvoiceAdjustment> {
    val out = mutableListOf<FreightInvoiceAdjustment>()
    for (item in items) {
        val derived = newFreightInvoiceAdjustment(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectFreightInvoiceAdjustment(item: FreightInvoiceAdjustment): Double {
    return primaryFreightInvoiceTemplateWeight(5, true)
}
