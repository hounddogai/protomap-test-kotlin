package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.freightpay.ratecard.preference.GlobalRateCardPreference
import com.acme.transport.freightpay.ratecard.preference.normalizeRollingRateCardThresholdCode

fun summarizeGlobalRateCardPreferencesForTemplate(items: MutableList<GlobalRateCardPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePendingFuelSurchargeTemplatesFromPreference(items: MutableList<GlobalRateCardPreference>): MutableList<PendingFuelSurchargeTemplate> {
    val out = mutableListOf<PendingFuelSurchargeTemplate>()
    for (item in items) {
        val derived = newPendingFuelSurchargeTemplate(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchPendingFuelSurchargeTemplate(item: PendingFuelSurchargeTemplate): String {
    return normalizeRollingRateCardThresholdCode(item.code)
}
