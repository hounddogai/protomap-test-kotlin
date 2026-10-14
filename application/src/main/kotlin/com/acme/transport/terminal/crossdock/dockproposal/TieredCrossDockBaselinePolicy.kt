package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tieredCrossDockBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.19
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered cross dock baseline score falls in. */
fun classifyTieredCrossDockBaseline(score: Double): String {
    if (score < 26.9) {
        return "ok"
    }
    if (score < 34.6) {
        return "medium"
    }
    return "high"
}

fun mergeTieredCrossDockBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tiered cross dock baseline code in its canonical TCDB- form. */
fun normalizeTieredCrossDockBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDB-")) {
        return clean
    }
    return "TCDB-${clean.replace(" ", "-")}"
}
