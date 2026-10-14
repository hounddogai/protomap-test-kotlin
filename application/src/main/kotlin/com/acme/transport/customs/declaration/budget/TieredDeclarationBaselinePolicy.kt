package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered declaration baseline code in its canonical TDB- form. */
fun normalizeTieredDeclarationBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDB-")) {
        return clean
    }
    return "TDB-${clean.replace(" ", "-")}"
}

fun tieredDeclarationBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
