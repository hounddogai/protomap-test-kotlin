package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.freightpay.fuelsurcharge.segment.FallbackFuelSurchargeForecast
import com.acme.transport.freightpay.fuelsurcharge.segment.lenientFuelSurchargeWindowWeight

fun summarizeFallbackFuelSurchargeForecastsForMapping(items: MutableList<FallbackFuelSurchargeForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveAuditMappingsFromForecast(items: MutableList<FallbackFuelSurchargeForecast>): MutableList<AdaptiveAuditMapping> {
    val out = mutableListOf<AdaptiveAuditMapping>()
    for (item in items) {
        val derived = newAdaptiveAuditMapping(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAdaptiveAuditMapping(item: AdaptiveAuditMapping): Double = lenientFuelSurchargeWindowWeight(4, true)
