package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.numbers.roundTo

/** Puts a lenient rate card manifest code in its canonical LRCM- form. */
fun normalizeLenientRateCardManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRCM-")) {
        return clean
    }
    return "LRCM-${clean.replace(" ", "-")}"
}

fun lenientRateCardManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
