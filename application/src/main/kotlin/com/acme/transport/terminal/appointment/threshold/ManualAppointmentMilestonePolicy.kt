package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a manual appointment milestone code in its canonical MAM- form. */
fun normalizeManualAppointmentMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MAM-")) {
        return clean
    }
    return "MAM-${clean.replace(" ", "-")}"
}

fun mergeManualAppointmentMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
