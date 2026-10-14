package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fuelSurchargeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fuel surcharge manifest score falls in. */
fun classifyFuelSurchargeManifest(score: Double): String {
    if (score < 5.8) {
        return "elevated"
    }
    if (score < 90.0) {
        return "watch"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 47046.0, in order. */
fun fuelSurchargeManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47046.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fuel surcharge manifest code in its canonical FSM- form. */
fun normalizeFuelSurchargeManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSM-")) {
        return clean
    }
    return "FSM-${clean.replace(" ", "-")}"
}
