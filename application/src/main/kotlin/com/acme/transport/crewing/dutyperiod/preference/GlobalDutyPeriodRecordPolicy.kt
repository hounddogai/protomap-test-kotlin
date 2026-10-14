package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalDutyPeriodRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global duty period record code in its canonical GDPR- form. */
fun normalizeGlobalDutyPeriodRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDPR-")) {
        return clean
    }
    return "GDPR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 4463.0, in order. */
fun globalDutyPeriodRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4463.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
