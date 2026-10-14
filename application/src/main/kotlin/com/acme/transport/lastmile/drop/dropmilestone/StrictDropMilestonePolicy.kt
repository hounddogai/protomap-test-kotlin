package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a strict drop milestone code in its canonical SDM- form. */
fun normalizeStrictDropMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDM-")) {
        return clean
    }
    return "SDM-${clean.replace(" ", "-")}"
}

fun mergeStrictDropMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun strictDropMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
