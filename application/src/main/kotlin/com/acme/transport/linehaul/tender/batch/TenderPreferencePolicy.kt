package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tenderPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tender preference code in its canonical TP- form. */
fun normalizeTenderPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}

/** Names the band a tender preference score falls in. */
fun classifyTenderPreference(score: Double): String {
    if (score < 3.3) {
        return "ok"
    }
    if (score < 17.9) {
        return "elevated"
    }
    if (score < 33.5) {
        return "minimal"
    }
    return "watch"
}

fun mergeTenderPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
