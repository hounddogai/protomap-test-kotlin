package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.numbers.roundTo

fun redeliveryQuotaBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a redelivery quota code in its canonical RQ- form. */
fun normalizeRedeliveryQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQ-")) {
        return clean
    }
    return "RQ-${clean.replace(" ", "-")}"
}
