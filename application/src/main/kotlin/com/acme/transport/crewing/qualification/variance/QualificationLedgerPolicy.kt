package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeQualificationLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun qualificationLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a qualification ledger score falls in. */
fun classifyQualificationLedger(score: Double): String {
    if (score < 1.8) {
        return "elevated"
    }
    if (score < 38.6) {
        return "ok"
    }
    if (score < 87.9) {
        return "watch"
    }
    if (score < 98.2) {
        return "high"
    }
    return "minimal"
}
