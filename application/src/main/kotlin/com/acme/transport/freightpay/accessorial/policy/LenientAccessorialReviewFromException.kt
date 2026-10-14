package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.fuelsurcharge.exception.FuelSurchargeException
import com.acme.transport.freightpay.fuelsurcharge.exception.normalizeFuelSurchargeSegmentCode

fun summarizeFuelSurchargeExceptionsForReview(items: MutableList<FuelSurchargeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientAccessorialReviewsFromException(items: MutableList<FuelSurchargeException>): MutableList<LenientAccessorialReview> {
    val out = mutableListOf<LenientAccessorialReview>()
    for (item in items) {
        val derived = newLenientAccessorialReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectLenientAccessorialReview(item: LenientAccessorialReview): String {
    return normalizeFuelSurchargeSegmentCode(item.code)
}
