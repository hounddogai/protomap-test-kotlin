package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched tender forecast code in its canonical BTF- form. */
fun normalizeBatchedTenderForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTF-")) {
        return clean
    }
    return "BTF-${clean.replace(" ", "-")}"
}

fun batchedTenderForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun batchedTenderForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
