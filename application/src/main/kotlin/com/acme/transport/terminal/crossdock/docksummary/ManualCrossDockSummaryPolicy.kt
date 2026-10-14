package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualCrossDockSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual cross dock summary code in its canonical MCDS- form. */
fun normalizeManualCrossDockSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCDS-")) {
        return clean
    }
    return "MCDS-${clean.replace(" ", "-")}"
}
