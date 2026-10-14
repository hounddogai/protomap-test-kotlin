package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun regionalDeclarationTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a regional declaration template score falls in. */
fun classifyRegionalDeclarationTemplate(score: Double): String {
    if (score < 10.8) {
        return "ok"
    }
    if (score < 57.8) {
        return "elevated"
    }
    return "high"
}

fun regionalDeclarationTemplateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun regionalDeclarationTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
