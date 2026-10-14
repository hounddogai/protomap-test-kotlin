package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun globalTenderProposalBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a global tender proposal code in its canonical GTP- form. */
fun normalizeGlobalTenderProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTP-")) {
        return clean
    }
    return "GTP-${clean.replace(" ", "-")}"
}

fun mergeGlobalTenderProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalTenderProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
