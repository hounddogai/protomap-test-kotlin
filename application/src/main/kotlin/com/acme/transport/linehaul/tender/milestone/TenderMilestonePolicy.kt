package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tenderMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tender milestone code in its canonical TM- form. */
fun normalizeTenderMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}
