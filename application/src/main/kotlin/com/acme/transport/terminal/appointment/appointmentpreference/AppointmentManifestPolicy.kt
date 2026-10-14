package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAppointmentManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a appointment manifest code in its canonical AM- form. */
fun normalizeAppointmentManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}
