package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.fuelsurcharge.variance.LenientFuelSurchargeBundle
import com.acme.transport.freightpay.fuelsurcharge.variance.fuelSurchargeReservationWeight

fun summarizeLenientFuelSurchargeBundlesForWindow(items: MutableList<LenientFuelSurchargeBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAuditWindowsFromBundle(items: MutableList<LenientFuelSurchargeBundle>): MutableList<AuditWindow> {
    val out = mutableListOf<AuditWindow>()
    for (item in items) {
        val derived = newAuditWindow(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankAuditWindow(item: AuditWindow): Double = fuelSurchargeReservationWeight(4, true)
