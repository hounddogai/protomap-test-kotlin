package com.acme.transport.crewing.qualification.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a qualification quota code in its canonical QQ- form. */
fun normalizeQualificationQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QQ-")) {
        return clean
    }
    return "QQ-${clean.replace(" ", "-")}"
}

fun qualificationQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 40098.0, in order. */
fun qualificationQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40098.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
