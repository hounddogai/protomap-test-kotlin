package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lockerAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a locker audit code in its canonical LA- form. */
fun normalizeLockerAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LA-")) {
        return clean
    }
    return "LA-${clean.replace(" ", "-")}"
}

fun lockerAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun lockerAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
