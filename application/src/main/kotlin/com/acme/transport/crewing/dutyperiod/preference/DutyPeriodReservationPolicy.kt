package com.acme.transport.crewing.dutyperiod.preference

/** Puts a duty period reservation code in its canonical DPR- form. */
fun normalizeDutyPeriodReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPR-")) {
        return clean
    }
    return "DPR-${clean.replace(" ", "-")}"
}

fun dutyPeriodReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
