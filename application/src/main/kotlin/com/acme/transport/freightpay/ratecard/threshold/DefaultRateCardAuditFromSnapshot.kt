package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.RegionalFuelSurchargeSnapshot
import com.acme.transport.freightpay.fuelsurcharge.surchargebaseline.regionalFuelSurchargeSnapshotBucket

fun summarizeRegionalFuelSurchargeSnapshotsForAudit(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveDefaultRateCardAuditsFromSnapshot(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableList<DefaultRateCardAudit> {
    val out = mutableListOf<DefaultRateCardAudit>()
    for (item in items) {
        val derived = newDefaultRateCardAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefaultRateCardAudit(item: DefaultRateCardAudit): String {
    return regionalFuelSurchargeSnapshotBucket(item.projectedVolume())
}
