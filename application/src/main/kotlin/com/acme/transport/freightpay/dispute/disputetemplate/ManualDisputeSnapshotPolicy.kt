package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a manual dispute snapshot score falls in. */
fun classifyManualDisputeSnapshot(score: Double): String {
    if (score < 15.6) {
        return "medium"
    }
    if (score < 34.0) {
        return "ok"
    }
    if (score < 40.1) {
        return "low"
    }
    if (score < 84.7) {
        return "minimal"
    }
    return "elevated"
}

/** Puts a manual dispute snapshot code in its canonical MDS- form. */
fun normalizeManualDisputeSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDS-")) {
        return clean
    }
    return "MDS-${clean.replace(" ", "-")}"
}

fun manualDisputeSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 12176.0, in order. */
fun manualDisputeSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
