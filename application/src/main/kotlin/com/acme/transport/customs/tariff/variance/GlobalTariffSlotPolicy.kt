package com.acme.transport.customs.tariff.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalTariffSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalTariffSlotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a global tariff slot code in its canonical GTS- form. */
fun normalizeGlobalTariffSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTS-")) {
        return clean
    }
    return "GTS-${clean.replace(" ", "-")}"
}
