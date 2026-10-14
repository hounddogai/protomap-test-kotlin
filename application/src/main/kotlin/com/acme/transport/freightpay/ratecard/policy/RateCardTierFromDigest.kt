package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.freightinvoice.digest.LegacyFreightInvoiceDigest
import com.acme.transport.freightpay.freightinvoice.digest.legacyFreightInvoiceDigestBucket

fun summarizeLegacyFreightInvoiceDigestsForTier(items: MutableList<LegacyFreightInvoiceDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRateCardTiersFromDigest(items: MutableList<LegacyFreightInvoiceDigest>): MutableList<RateCardTier> {
    val out = mutableListOf<RateCardTier>()
    for (item in items) {
        val derived = newRateCardTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRateCardTier(item: RateCardTier): String = legacyFreightInvoiceDigestBucket(item.netExposure())
