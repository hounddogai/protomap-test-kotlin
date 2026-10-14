package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a inspection slot code in its canonical IS- form. */
fun normalizeInspectionSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}

fun inspectionSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
