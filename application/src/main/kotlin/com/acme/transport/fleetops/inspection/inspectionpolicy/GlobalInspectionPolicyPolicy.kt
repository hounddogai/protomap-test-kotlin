package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.numbers.roundTo

fun globalInspectionPolicyBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a global inspection policy code in its canonical GIP- form. */
fun normalizeGlobalInspectionPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GIP-")) {
        return clean
    }
    return "GIP-${clean.replace(" ", "-")}"
}
