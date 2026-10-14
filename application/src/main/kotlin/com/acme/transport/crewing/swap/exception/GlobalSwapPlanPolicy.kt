package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalSwapPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalSwapPlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a global swap plan code in its canonical GSP- form. */
fun normalizeGlobalSwapPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GSP-")) {
        return clean
    }
    return "GSP-${clean.replace(" ", "-")}"
}
