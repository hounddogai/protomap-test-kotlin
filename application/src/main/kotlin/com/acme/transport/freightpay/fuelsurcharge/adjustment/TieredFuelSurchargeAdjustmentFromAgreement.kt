package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.freightinvoice.rule.FreightInvoiceAgreement
import com.acme.transport.freightpay.freightinvoice.rule.freightInvoiceRuleWeight

fun summarizeFreightInvoiceAgreementsForAdjustment(items: MutableList<FreightInvoiceAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveTieredFuelSurchargeAdjustmentsFromAgreement(items: MutableList<FreightInvoiceAgreement>): MutableList<TieredFuelSurchargeAdjustment> {
    val out = mutableListOf<TieredFuelSurchargeAdjustment>()
    for (item in items) {
        val derived = newTieredFuelSurchargeAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankTieredFuelSurchargeAdjustment(item: TieredFuelSurchargeAdjustment): Double {
    return freightInvoiceRuleWeight(1, true)
}
