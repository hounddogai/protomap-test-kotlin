package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 30718.0, in order. */
fun pendingDeclarationSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30718.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending declaration snapshot code in its canonical PDS- form. */
fun normalizePendingDeclarationSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDS-")) {
        return clean
    }
    return "PDS-${clean.replace(" ", "-")}"
}

fun pendingDeclarationSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a pending declaration snapshot score falls in. */
fun classifyPendingDeclarationSnapshot(score: Double): String {
    if (score < 56.9) {
        return "high"
    }
    if (score < 64.7) {
        return "watch"
    }
    return "critical"
}
