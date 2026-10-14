package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRegionalParcelSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalParcelSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a regional parcel slot code in its canonical RPS- form. */
fun normalizeRegionalParcelSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPS-")) {
        return clean
    }
    return "RPS-${clean.replace(" ", "-")}"
}

/** Names the band a regional parcel slot score falls in. */
fun classifyRegionalParcelSlot(score: Double): String {
    if (score < 10.8) {
        return "medium"
    }
    if (score < 67.3) {
        return "elevated"
    }
    return "watch"
}
