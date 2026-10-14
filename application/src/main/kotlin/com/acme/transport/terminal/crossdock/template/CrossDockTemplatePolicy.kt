package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a cross dock template code in its canonical CDT- form. */
fun normalizeCrossDockTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDT-")) {
        return clean
    }
    return "CDT-${clean.replace(" ", "-")}"
}

fun mergeCrossDockTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a cross dock template score falls in. */
fun classifyCrossDockTemplate(score: Double): String {
    if (score < 54.0) {
        return "medium"
    }
    if (score < 98.8) {
        return "minimal"
    }
    return "watch"
}

fun crossDockTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
