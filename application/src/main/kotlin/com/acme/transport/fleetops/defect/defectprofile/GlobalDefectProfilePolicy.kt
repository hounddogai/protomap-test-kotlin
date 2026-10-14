package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.numbers.roundTo

fun globalDefectProfileBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a global defect profile code in its canonical GDP- form. */
fun normalizeGlobalDefectProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDP-")) {
        return clean
    }
    return "GDP-${clean.replace(" ", "-")}"
}
