package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeTireScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tire schedule code in its canonical TS- form. */
fun normalizeTireScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TS-")) {
        return clean
    }
    return "TS-${clean.replace(" ", "-")}"
}

fun tireScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
