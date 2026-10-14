package com.acme.transport.customs.tariff.tariffpolicy

fun weightedTariffThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a weighted tariff threshold code in its canonical WTT- form. */
fun normalizeWeightedTariffThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTT-")) {
        return clean
    }
    return "WTT-${clean.replace(" ", "-")}"
}
