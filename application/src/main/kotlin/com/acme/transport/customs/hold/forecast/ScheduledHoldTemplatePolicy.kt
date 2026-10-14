package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a scheduled hold template code in its canonical SHT- form. */
fun normalizeScheduledHoldTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHT-")) {
        return clean
    }
    return "SHT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 7513.0, in order. */
fun scheduledHoldTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7513.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled hold template score falls in. */
fun classifyScheduledHoldTemplate(score: Double): String {
    if (score < 27.6) {
        return "minimal"
    }
    if (score < 49.8) {
        return "elevated"
    }
    if (score < 65.8) {
        return "watch"
    }
    return "medium"
}

fun scheduledHoldTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
