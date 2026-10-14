package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a fuel surcharge milestone score falls in. */
fun classifyFuelSurchargeMilestone(score: Double): String {
    if (score < 20.4) {
        return "low"
    }
    if (score < 38.8) {
        return "critical"
    }
    if (score < 77.4) {
        return "elevated"
    }
    if (score < 95.6) {
        return "high"
    }
    return "ok"
}

fun fuelSurchargeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fuel surcharge milestone code in its canonical FSM- form. */
fun normalizeFuelSurchargeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSM-")) {
        return clean
    }
    return "FSM-${clean.replace(" ", "-")}"
}

fun mergeFuelSurchargeMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
