package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.numbers.roundTo

/** Puts a global defect snapshot code in its canonical GDS- form. */
fun normalizeGlobalDefectSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDS-")) {
        return clean
    }
    return "GDS-${clean.replace(" ", "-")}"
}

fun globalDefectSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
