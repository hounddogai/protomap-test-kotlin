package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a inspection allocation code in its canonical IA- form. */
fun normalizeInspectionAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IA-")) {
        return clean
    }
    return "IA-${clean.replace(" ", "-")}"
}

fun inspectionAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
