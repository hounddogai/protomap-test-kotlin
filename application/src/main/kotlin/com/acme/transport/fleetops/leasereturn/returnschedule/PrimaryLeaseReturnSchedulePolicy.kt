package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a primary lease return schedule code in its canonical PLRS- form. */
fun normalizePrimaryLeaseReturnScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLRS-")) {
        return clean
    }
    return "PLRS-${clean.replace(" ", "-")}"
}

fun primaryLeaseReturnScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
