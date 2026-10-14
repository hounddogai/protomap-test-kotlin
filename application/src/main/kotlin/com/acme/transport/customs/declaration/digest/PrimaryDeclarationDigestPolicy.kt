package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergePrimaryDeclarationDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary declaration digest score falls in. */
fun classifyPrimaryDeclarationDigest(score: Double): String {
    if (score < 6.0) {
        return "high"
    }
    if (score < 56.4) {
        return "medium"
    }
    if (score < 94.6) {
        return "critical"
    }
    return "elevated"
}

fun primaryDeclarationDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
