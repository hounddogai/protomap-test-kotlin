package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.numbers.roundTo

/** Puts a global cross dock preference code in its canonical GCDP- form. */
fun normalizeGlobalCrossDockPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCDP-")) {
        return clean
    }
    return "GCDP-${clean.replace(" ", "-")}"
}

fun globalCrossDockPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
