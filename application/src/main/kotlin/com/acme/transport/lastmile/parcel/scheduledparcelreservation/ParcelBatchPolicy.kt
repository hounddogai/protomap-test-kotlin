package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun parcelBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a parcel batch code in its canonical PB- form. */
fun normalizeParcelBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PB-")) {
        return clean
    }
    return "PB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 64508.0, in order. */
fun parcelBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64508.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
