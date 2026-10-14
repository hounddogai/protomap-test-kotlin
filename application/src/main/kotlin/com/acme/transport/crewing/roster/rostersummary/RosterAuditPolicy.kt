package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a roster audit score falls in. */
fun classifyRosterAudit(score: Double): String {
    if (score < 17.7) {
        return "medium"
    }
    if (score < 46.1) {
        return "watch"
    }
    return "minimal"
}

/** Puts a roster audit code in its canonical RA- form. */
fun normalizeRosterAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

fun rosterAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 84205.0, in order. */
fun rosterAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84205.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
