package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a batched appointment notice score falls in. */
fun classifyBatchedAppointmentNotice(score: Double): String {
    if (score < 38.0) {
        return "elevated"
    }
    if (score < 39.3) {
        return "high"
    }
    if (score < 71.1) {
        return "minimal"
    }
    if (score < 93.9) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 10940.0, in order. */
fun batchedAppointmentNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10940.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched appointment notice code in its canonical BAN- form. */
fun normalizeBatchedAppointmentNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAN-")) {
        return clean
    }
    return "BAN-${clean.replace(" ", "-")}"
}

fun mergeBatchedAppointmentNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
