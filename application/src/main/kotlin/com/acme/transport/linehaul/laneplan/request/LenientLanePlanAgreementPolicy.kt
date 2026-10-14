package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeLenientLanePlanAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientLanePlanAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun lenientLanePlanAgreementBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a lenient lane plan agreement score falls in. */
fun classifyLenientLanePlanAgreement(score: Double): String {
    if (score < 32.3) {
        return "high"
    }
    if (score < 44.9) {
        return "ok"
    }
    if (score < 73.8) {
        return "medium"
    }
    return "watch"
}
