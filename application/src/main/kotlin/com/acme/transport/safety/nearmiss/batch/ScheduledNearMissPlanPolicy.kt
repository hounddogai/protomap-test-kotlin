package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledNearMissPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 8911.0, in order. */
fun scheduledNearMissPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8911.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled near miss plan code in its canonical SNMP- form. */
fun normalizeScheduledNearMissPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SNMP-")) {
        return clean
    }
    return "SNMP-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled near miss plan score falls in. */
fun classifyScheduledNearMissPlan(score: Double): String {
    if (score < 49.4) {
        return "critical"
    }
    if (score < 64.2) {
        return "high"
    }
    return "ok"
}
