package com.acme.transport.lastmile.drop.draft

import com.acme.transport.kit.numbers.roundTo

fun dropDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a drop draft code in its canonical DD- form. */
fun normalizeDropDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DD-")) {
        return clean
    }
    return "DD-${clean.replace(" ", "-")}"
}
