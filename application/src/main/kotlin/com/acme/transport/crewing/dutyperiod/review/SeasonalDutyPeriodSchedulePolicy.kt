package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.numbers.roundTo

/** Puts a seasonal duty period schedule code in its canonical SDPS- form. */
fun normalizeSeasonalDutyPeriodScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDPS-")) {
        return clean
    }
    return "SDPS-${clean.replace(" ", "-")}"
}

fun seasonalDutyPeriodScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun seasonalDutyPeriodScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
