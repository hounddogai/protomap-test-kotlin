package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.numbers.roundTo

fun regionalFuelSurchargeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a regional fuel surcharge snapshot code in its canonical RFSS- form. */
fun normalizeRegionalFuelSurchargeSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFSS-")) {
        return clean
    }
    return "RFSS-${clean.replace(" ", "-")}"
}
