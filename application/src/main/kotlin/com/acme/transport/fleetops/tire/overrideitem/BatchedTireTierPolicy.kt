package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun batchedTireTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeBatchedTireTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched tire tier code in its canonical BTT- form. */
fun normalizeBatchedTireTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTT-")) {
        return clean
    }
    return "BTT-${clean.replace(" ", "-")}"
}

/** Names the band a batched tire tier score falls in. */
fun classifyBatchedTireTier(score: Double): String {
    if (score < 2.1) {
        return "low"
    }
    if (score < 26.4) {
        return "medium"
    }
    if (score < 65.4) {
        return "high"
    }
    if (score < 90.6) {
        return "elevated"
    }
    return "ok"
}
