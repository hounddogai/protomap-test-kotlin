package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRegionalRateCardSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a regional rate card snapshot score falls in. */
fun classifyRegionalRateCardSnapshot(score: Double): String {
    if (score < 32.3) {
        return "minimal"
    }
    if (score < 81.0) {
        return "elevated"
    }
    return "ok"
}

/** Puts a regional rate card snapshot code in its canonical RRCS- form. */
fun normalizeRegionalRateCardSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRCS-")) {
        return clean
    }
    return "RRCS-${clean.replace(" ", "-")}"
}

fun regionalRateCardSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
