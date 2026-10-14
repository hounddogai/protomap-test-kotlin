package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a audit window code in its canonical AW- form. */
fun normalizeAuditWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AW-")) {
        return clean
    }
    return "AW-${clean.replace(" ", "-")}"
}

/** Names the band a audit window score falls in. */
fun classifyAuditWindow(score: Double): String {
    if (score < 8.7) {
        return "minimal"
    }
    if (score < 51.6) {
        return "elevated"
    }
    if (score < 73.4) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 38481.0, in order. */
fun auditWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38481.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun auditWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
