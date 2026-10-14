package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.numbers.roundTo

/** Puts a lane plan notice code in its canonical LPN- form. */
fun normalizeLanePlanNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPN-")) {
        return clean
    }
    return "LPN-${clean.replace(" ", "-")}"
}

fun lanePlanNoticeBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
