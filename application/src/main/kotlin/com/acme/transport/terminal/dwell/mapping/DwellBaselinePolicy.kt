package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a dwell baseline score falls in. */
fun classifyDwellBaseline(score: Double): String {
    if (score < 1.4) {
        return "medium"
    }
    if (score < 17.6) {
        return "ok"
    }
    if (score < 63.1) {
        return "elevated"
    }
    if (score < 64.0) {
        return "high"
    }
    return "watch"
}

fun mergeDwellBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun dwellBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dwell baseline code in its canonical DB- form. */
fun normalizeDwellBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DB-")) {
        return clean
    }
    return "DB-${clean.replace(" ", "-")}"
}
