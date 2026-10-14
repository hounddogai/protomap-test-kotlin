package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeSeasonalFuelSurchargeBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalFuelSurchargeBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a seasonal fuel surcharge batch code in its canonical SFSB- form. */
fun normalizeSeasonalFuelSurchargeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFSB-")) {
        return clean
    }
    return "SFSB-${clean.replace(" ", "-")}"
}

fun seasonalFuelSurchargeBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
