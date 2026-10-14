package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedTariffPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched tariff policy code in its canonical BTP- form. */
fun normalizeBatchedTariffPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BTP-")) {
        return clean
    }
    return "BTP-${clean.replace(" ", "-")}"
}
