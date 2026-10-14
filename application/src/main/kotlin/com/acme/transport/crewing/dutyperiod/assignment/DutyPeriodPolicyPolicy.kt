package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a duty period policy code in its canonical DPP- form. */
fun normalizeDutyPeriodPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPP-")) {
        return clean
    }
    return "DPP-${clean.replace(" ", "-")}"
}

fun dutyPeriodPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun dutyPeriodPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
