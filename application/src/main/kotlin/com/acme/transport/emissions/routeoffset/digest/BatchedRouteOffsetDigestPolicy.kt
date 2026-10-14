package com.acme.transport.emissions.routeoffset.digest

fun batchedRouteOffsetDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a batched route offset digest code in its canonical BROD- form. */
fun normalizeBatchedRouteOffsetDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BROD-")) {
        return clean
    }
    return "BROD-${clean.replace(" ", "-")}"
}
