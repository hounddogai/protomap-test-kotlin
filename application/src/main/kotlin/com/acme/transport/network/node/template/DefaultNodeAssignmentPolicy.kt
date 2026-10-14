package com.acme.transport.network.node.template

import com.acme.transport.kit.numbers.roundTo

fun defaultNodeAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a default node assignment code in its canonical DNA- form. */
fun normalizeDefaultNodeAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DNA-")) {
        return clean
    }
    return "DNA-${clean.replace(" ", "-")}"
}
