package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a strict fuel surcharge target score falls in. */
fun classifyStrictFuelSurchargeTarget(score: Double): String {
    if (score < 33.0) {
        return "elevated"
    }
    if (score < 42.5) {
        return "critical"
    }
    if (score < 79.7) {
        return "medium"
    }
    return "ok"
}

fun mergeStrictFuelSurchargeTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
