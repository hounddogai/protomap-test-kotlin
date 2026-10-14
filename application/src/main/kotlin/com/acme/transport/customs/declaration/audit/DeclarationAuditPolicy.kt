package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a declaration audit score falls in. */
fun classifyDeclarationAudit(score: Double): String {
    if (score < 10.1) {
        return "elevated"
    }
    if (score < 18.8) {
        return "critical"
    }
    if (score < 30.5) {
        return "minimal"
    }
    if (score < 82.4) {
        return "ok"
    }
    return "low"
}

fun declarationAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun declarationAuditBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun declarationAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
