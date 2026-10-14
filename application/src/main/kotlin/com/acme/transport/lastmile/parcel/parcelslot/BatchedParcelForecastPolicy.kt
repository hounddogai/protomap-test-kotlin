package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched parcel forecast code in its canonical BPF- form. */
fun normalizeBatchedParcelForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BPF-")) {
        return clean
    }
    return "BPF-${clean.replace(" ", "-")}"
}

fun batchedParcelForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
