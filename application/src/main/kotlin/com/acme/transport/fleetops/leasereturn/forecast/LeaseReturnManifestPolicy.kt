package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.numbers.roundTo

/** Puts a lease return manifest code in its canonical LRM- form. */
fun normalizeLeaseReturnManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRM-")) {
        return clean
    }
    return "LRM-${clean.replace(" ", "-")}"
}

fun leaseReturnManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
