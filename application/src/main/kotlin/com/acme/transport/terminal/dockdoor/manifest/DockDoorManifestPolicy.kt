package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 17087.0, in order. */
fun dockDoorManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17087.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dockDoorManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a dock door manifest code in its canonical DDM- form. */
fun normalizeDockDoorManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDM-")) {
        return clean
    }
    return "DDM-${clean.replace(" ", "-")}"
}

fun dockDoorManifestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
