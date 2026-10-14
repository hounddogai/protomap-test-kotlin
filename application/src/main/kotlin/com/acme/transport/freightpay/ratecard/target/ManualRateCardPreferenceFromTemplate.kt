package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.fuelsurcharge.notice.PendingFuelSurchargeTemplate
import com.acme.transport.freightpay.fuelsurcharge.notice.tieredFuelSurchargeNoticeWeight

fun summarizePendingFuelSurchargeTemplatesForPreference(items: MutableList<PendingFuelSurchargeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveManualRateCardPreferencesFromTemplate(items: MutableList<PendingFuelSurchargeTemplate>): MutableList<ManualRateCardPreference> {
    val out = mutableListOf<ManualRateCardPreference>()
    for (item in items) {
        val derived = newManualRateCardPreference(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualRateCardPreference(item: ManualRateCardPreference): Double {
    return tieredFuelSurchargeNoticeWeight(2, true)
}
