package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lenientCoachingTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLenientCoachingTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient coaching template code in its canonical LCT- form. */
fun normalizeLenientCoachingTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCT-")) {
        return clean
    }
    return "LCT-${clean.replace(" ", "-")}"
}

/** Names the band a lenient coaching template score falls in. */
fun classifyLenientCoachingTemplate(score: Double): String {
    if (score < 9.0) {
        return "ok"
    }
    if (score < 46.8) {
        return "watch"
    }
    return "low"
}
