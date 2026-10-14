package com.acme.transport.safety.nearmiss.metric

/** Puts a near miss metric code in its canonical NMM- form. */
fun normalizeNearMissMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMM-")) {
        return clean
    }
    return "NMM-${clean.replace(" ", "-")}"
}

fun nearMissMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
