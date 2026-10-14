package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.numbers.roundTo

/** Puts a batched fuel burn cycle code in its canonical BFBC- form. */
fun normalizeBatchedFuelBurnCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BFBC-")) {
        return clean
    }
    return "BFBC-${clean.replace(" ", "-")}"
}

fun batchedFuelBurnCycleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
