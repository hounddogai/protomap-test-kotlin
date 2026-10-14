package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun rateCardNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a rate card notice code in its canonical RCN- form. */
fun normalizeRateCardNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCN-")) {
        return clean
    }
    return "RCN-${clean.replace(" ", "-")}"
}

fun mergeRateCardNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rate card notice score falls in. */
fun classifyRateCardNotice(score: Double): String {
    if (score < 24.8) {
        return "low"
    }
    if (score < 36.5) {
        return "ok"
    }
    if (score < 50.1) {
        return "critical"
    }
    if (score < 84.5) {
        return "minimal"
    }
    return "medium"
}
