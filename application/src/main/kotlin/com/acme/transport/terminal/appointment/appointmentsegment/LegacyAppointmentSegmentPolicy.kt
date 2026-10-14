package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyAppointmentSegmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeLegacyAppointmentSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy appointment segment code in its canonical LAS- form. */
fun normalizeLegacyAppointmentSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAS-")) {
        return clean
    }
    return "LAS-${clean.replace(" ", "-")}"
}
