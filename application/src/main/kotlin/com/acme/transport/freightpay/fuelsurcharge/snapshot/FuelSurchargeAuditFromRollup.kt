package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.freightinvoice.rollup.FreightInvoiceRollup
import com.acme.transport.freightpay.freightinvoice.rollup.classifyFreightInvoiceRollup

fun summarizeFreightInvoiceRollupsForAudit(items: MutableList<FreightInvoiceRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveFuelSurchargeAuditsFromRollup(items: MutableList<FreightInvoiceRollup>): MutableList<FuelSurchargeAudit> {
    val out = mutableListOf<FuelSurchargeAudit>()
    for (item in items) {
        val derived = newFuelSurchargeAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceFuelSurchargeAudit(item: FuelSurchargeAudit): String {
    return classifyFreightInvoiceRollup(item.measuredValue())
}
