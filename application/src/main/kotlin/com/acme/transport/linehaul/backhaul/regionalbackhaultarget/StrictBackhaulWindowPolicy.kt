package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a strict backhaul window score falls in. */
fun classifyStrictBackhaulWindow(score: Double): String {
    if (score < 39.7) {
        return "low"
    }
    if (score < 57.5) {
        return "minimal"
    }
    if (score < 71.6) {
        return "critical"
    }
    return "elevated"
}

fun mergeStrictBackhaulWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a strict backhaul window code in its canonical SBW- form. */
fun normalizeStrictBackhaulWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBW-")) {
        return clean
    }
    return "SBW-${clean.replace(" ", "-")}"
}

fun strictBackhaulWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
