package com.acme.transport.customs.inspection.schedule

fun batchedInspectionTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a batched inspection template code in its canonical BIT- form. */
fun normalizeBatchedInspectionTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BIT-")) {
        return clean
    }
    return "BIT-${clean.replace(" ", "-")}"
}

/** Names the band a batched inspection template score falls in. */
fun classifyBatchedInspectionTemplate(score: Double): String {
    if (score < 3.8) {
        return "watch"
    }
    if (score < 28.8) {
        return "medium"
    }
    if (score < 77.8) {
        return "low"
    }
    return "ok"
}
