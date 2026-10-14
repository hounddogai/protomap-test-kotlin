package com.acme.transport.crewing.dutyperiod.periodreview

fun fallbackDutyPeriodOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a fallback duty period override code in its canonical FDPO- form. */
fun normalizeFallbackDutyPeriodOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDPO-")) {
        return clean
    }
    return "FDPO-${clean.replace(" ", "-")}"
}
