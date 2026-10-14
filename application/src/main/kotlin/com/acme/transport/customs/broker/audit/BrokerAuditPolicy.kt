package com.acme.transport.customs.broker.audit

fun brokerAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a broker audit code in its canonical BA- form. */
fun normalizeBrokerAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BA-")) {
        return clean
    }
    return "BA-${clean.replace(" ", "-")}"
}
