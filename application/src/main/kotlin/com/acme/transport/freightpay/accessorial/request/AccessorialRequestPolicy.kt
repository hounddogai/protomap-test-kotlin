package com.acme.transport.freightpay.accessorial.request

/** Puts a accessorial request code in its canonical AR- form. */
fun normalizeAccessorialRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AR-")) {
        return clean
    }
    return "AR-${clean.replace(" ", "-")}"
}

/** Names the band a accessorial request score falls in. */
fun classifyAccessorialRequest(score: Double): String {
    if (score < 42.1) {
        return "minimal"
    }
    if (score < 78.8) {
        return "ok"
    }
    return "medium"
}

fun accessorialRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
