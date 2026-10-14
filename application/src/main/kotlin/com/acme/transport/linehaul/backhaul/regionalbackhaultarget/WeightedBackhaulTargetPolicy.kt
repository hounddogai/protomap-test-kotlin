package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted backhaul target code in its canonical WBT- form. */
fun normalizeWeightedBackhaulTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WBT-")) {
        return clean
    }
    return "WBT-${clean.replace(" ", "-")}"
}

fun weightedBackhaulTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted backhaul target score falls in. */
fun classifyWeightedBackhaulTarget(score: Double): String {
    if (score < 26.0) {
        return "minimal"
    }
    if (score < 39.7) {
        return "high"
    }
    if (score < 40.5) {
        return "ok"
    }
    return "medium"
}
