package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingFreightInvoiceScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun pendingFreightInvoiceScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending freight invoice schedule code in its canonical PFIS- form. */
fun normalizePendingFreightInvoiceScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFIS-")) {
        return clean
    }
    return "PFIS-${clean.replace(" ", "-")}"
}

/** Names the band a pending freight invoice schedule score falls in. */
fun classifyPendingFreightInvoiceSchedule(score: Double): String {
    if (score < 15.0) {
        return "minimal"
    }
    if (score < 91.2) {
        return "ok"
    }
    if (score < 99.5) {
        return "low"
    }
    return "watch"
}
