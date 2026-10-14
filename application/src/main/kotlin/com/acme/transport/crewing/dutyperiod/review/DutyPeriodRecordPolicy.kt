package com.acme.transport.crewing.dutyperiod.review

/** Puts a duty period record code in its canonical DPR- form. */
fun normalizeDutyPeriodRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPR-")) {
        return clean
    }
    return "DPR-${clean.replace(" ", "-")}"
}

fun dutyPeriodRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
