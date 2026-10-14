package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lockerTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a locker template score falls in. */
fun classifyLockerTemplate(score: Double): String {
    if (score < 36.7) {
        return "elevated"
    }
    if (score < 44.6) {
        return "watch"
    }
    return "high"
}

fun lockerTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a locker template code in its canonical LT- form. */
fun normalizeLockerTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LT-")) {
        return clean
    }
    return "LT-${clean.replace(" ", "-")}"
}
