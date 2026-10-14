package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryParcelDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary parcel draft score falls in. */
fun classifyPrimaryParcelDraft(score: Double): String {
    if (score < 36.0) {
        return "ok"
    }
    if (score < 69.1) {
        return "minimal"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 33055.0, in order. */
fun primaryParcelDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33055.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary parcel draft code in its canonical PPD- form. */
fun normalizePrimaryParcelDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PPD-")) {
        return clean
    }
    return "PPD-${clean.replace(" ", "-")}"
}
