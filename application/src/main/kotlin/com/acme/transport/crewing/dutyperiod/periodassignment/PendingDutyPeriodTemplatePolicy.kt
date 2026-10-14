package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 37738.0, in order. */
fun pendingDutyPeriodTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37738.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingDutyPeriodTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending duty period template score falls in. */
fun classifyPendingDutyPeriodTemplate(score: Double): String {
    if (score < 66.9) {
        return "ok"
    }
    if (score < 74.0) {
        return "watch"
    }
    return "medium"
}

/** Puts a pending duty period template code in its canonical PDPT- form. */
fun normalizePendingDutyPeriodTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDPT-")) {
        return clean
    }
    return "PDPT-${clean.replace(" ", "-")}"
}
