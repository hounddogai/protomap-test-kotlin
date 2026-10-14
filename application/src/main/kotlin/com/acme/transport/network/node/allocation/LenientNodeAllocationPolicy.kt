package com.acme.transport.network.node.allocation

import com.acme.transport.kit.numbers.roundTo

/** Puts a lenient node allocation code in its canonical LNA- form. */
fun normalizeLenientNodeAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LNA-")) {
        return clean
    }
    return "LNA-${clean.replace(" ", "-")}"
}

fun lenientNodeAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
