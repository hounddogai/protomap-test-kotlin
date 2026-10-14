package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict clearance schedule code in its canonical SCS- form. */
fun normalizeStrictClearanceScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCS-")) {
        return clean
    }
    return "SCS-${clean.replace(" ", "-")}"
}

fun strictClearanceScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
