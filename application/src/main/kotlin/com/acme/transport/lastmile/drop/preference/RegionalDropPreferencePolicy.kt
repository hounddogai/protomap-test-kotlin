package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRegionalDropPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a regional drop preference code in its canonical RDP- form. */
fun normalizeRegionalDropPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDP-")) {
        return clean
    }
    return "RDP-${clean.replace(" ", "-")}"
}

fun regionalDropPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun regionalDropPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
