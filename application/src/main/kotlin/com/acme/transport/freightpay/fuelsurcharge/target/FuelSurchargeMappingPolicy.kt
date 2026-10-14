package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fuelSurchargeMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeFuelSurchargeMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fuel surcharge mapping code in its canonical FSM- form. */
fun normalizeFuelSurchargeMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSM-")) {
        return clean
    }
    return "FSM-${clean.replace(" ", "-")}"
}

/** Names the band a fuel surcharge mapping score falls in. */
fun classifyFuelSurchargeMapping(score: Double): String {
    if (score < 4.4) {
        return "low"
    }
    if (score < 66.5) {
        return "high"
    }
    if (score < 66.5) {
        return "minimal"
    }
    if (score < 97.3) {
        return "ok"
    }
    return "critical"
}
