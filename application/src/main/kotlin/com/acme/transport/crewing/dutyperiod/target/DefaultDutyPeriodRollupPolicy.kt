package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a default duty period rollup code in its canonical DDPR- form. */
fun normalizeDefaultDutyPeriodRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDPR-")) {
        return clean
    }
    return "DDPR-${clean.replace(" ", "-")}"
}

fun defaultDutyPeriodRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
