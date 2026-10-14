package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.accessorial.quota.AccessorialProfile
import com.acme.transport.freightpay.accessorial.quota.normalizeAccessorialProfileCode

fun summarizeAccessorialProfilesForNotice(items: MutableList<AccessorialProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveWeightedFuelSurchargeNoticesFromProfile(items: MutableList<AccessorialProfile>): MutableList<WeightedFuelSurchargeNotice> {
    val out = mutableListOf<WeightedFuelSurchargeNotice>()
    for (item in items) {
        val derived = newWeightedFuelSurchargeNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeWeightedFuelSurchargeNotice(item: WeightedFuelSurchargeNotice): String {
    return normalizeAccessorialProfileCode(item.code)
}
