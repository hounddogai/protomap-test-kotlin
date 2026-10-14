package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeSwapExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a swap exception code in its canonical SE- form. */
fun normalizeSwapExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SE-")) {
        return clean
    }
    return "SE-${clean.replace(" ", "-")}"
}

/** Names the band a swap exception score falls in. */
fun classifySwapException(score: Double): String {
    if (score < 23.4) {
        return "watch"
    }
    if (score < 33.3) {
        return "low"
    }
    if (score < 37.3) {
        return "medium"
    }
    return "ok"
}

fun swapExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
