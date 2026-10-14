package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.kit.numbers.roundTo

fun dutyPeriodMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun dutyPeriodMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a duty period mapping code in its canonical DPM- form. */
fun normalizeDutyPeriodMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPM-")) {
        return clean
    }
    return "DPM-${clean.replace(" ", "-")}"
}
