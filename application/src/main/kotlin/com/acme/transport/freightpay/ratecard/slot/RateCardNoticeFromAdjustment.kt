package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.fuelsurcharge.adjustment.TieredFuelSurchargeAdjustment
import com.acme.transport.freightpay.fuelsurcharge.adjustment.fuelSurchargeQuotaBucket

fun summarizeTieredFuelSurchargeAdjustmentsForNotice(items: MutableList<TieredFuelSurchargeAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRateCardNoticesFromAdjustment(items: MutableList<TieredFuelSurchargeAdjustment>): MutableList<RateCardNotice> {
    val out = mutableListOf<RateCardNotice>()
    for (item in items) {
        val derived = newRateCardNotice(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyRateCardNotice(item: RateCardNotice): String = fuelSurchargeQuotaBucket(item.effectiveExposure())
