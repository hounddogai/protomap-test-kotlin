package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalQualificationAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a seasonal qualification allocation code in its canonical SQA- form. */
fun normalizeSeasonalQualificationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SQA-")) {
        return clean
    }
    return "SQA-${clean.replace(" ", "-")}"
}
