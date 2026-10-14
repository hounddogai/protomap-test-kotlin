package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun regionalTariffRollupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun regionalTariffRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeRegionalTariffRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
