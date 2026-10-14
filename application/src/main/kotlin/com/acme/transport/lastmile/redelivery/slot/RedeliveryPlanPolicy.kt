package com.acme.transport.lastmile.redelivery.slot

/** Puts a redelivery plan code in its canonical RP- form. */
fun normalizeRedeliveryPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RP-")) {
        return clean
    }
    return "RP-${clean.replace(" ", "-")}"
}

fun redeliveryPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
