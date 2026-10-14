package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeTenderDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tender draft code in its canonical TD- form. */
fun normalizeTenderDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TD-")) {
        return clean
    }
    return "TD-${clean.replace(" ", "-")}"
}

fun tenderDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
