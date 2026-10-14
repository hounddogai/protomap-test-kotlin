package com.acme.transport.network.zone.zonebundle

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy zone bundle code in its canonical LZB- form. */
fun normalizeLegacyZoneBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LZB-")) {
        return clean
    }
    return "LZB-${clean.replace(" ", "-")}"
}

fun legacyZoneBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
