package com.acme.transport.customs.hold.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a default hold override score falls in. */
fun classifyDefaultHoldOverride(score: Double): String {
    if (score < 35.0) {
        return "medium"
    }
    if (score < 46.9) {
        return "watch"
    }
    return "critical"
}

fun mergeDefaultHoldOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default hold override code in its canonical DHO- form. */
fun normalizeDefaultHoldOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DHO-")) {
        return clean
    }
    return "DHO-${clean.replace(" ", "-")}"
}

fun defaultHoldOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
