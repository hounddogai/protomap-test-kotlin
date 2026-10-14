package com.acme.transport.network.node.plan

import com.acme.transport.kit.numbers.roundTo

/** Puts a node assignment code in its canonical NA- form. */
fun normalizeNodeAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NA-")) {
        return clean
    }
    return "NA-${clean.replace(" ", "-")}"
}

fun nodeAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
