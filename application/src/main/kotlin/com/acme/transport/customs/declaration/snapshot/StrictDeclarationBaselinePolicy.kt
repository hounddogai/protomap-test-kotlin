package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.numbers.roundTo

fun strictDeclarationBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a strict declaration baseline code in its canonical SDB- form. */
fun normalizeStrictDeclarationBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDB-")) {
        return clean
    }
    return "SDB-${clean.replace(" ", "-")}"
}
