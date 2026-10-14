package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a idling template score falls in. */
fun classifyIdlingTemplate(score: Double): String {
    if (score < 10.4) {
        return "medium"
    }
    if (score < 72.5) {
        return "watch"
    }
    return "ok"
}

/** Puts a idling template code in its canonical IT- form. */
fun normalizeIdlingTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IT-")) {
        return clean
    }
    return "IT-${clean.replace(" ", "-")}"
}

fun idlingTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun idlingTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
