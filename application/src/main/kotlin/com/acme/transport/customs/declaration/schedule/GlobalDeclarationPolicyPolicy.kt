package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.numbers.roundTo

/** Puts a global declaration policy code in its canonical GDP- form. */
fun normalizeGlobalDeclarationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDP-")) {
        return clean
    }
    return "GDP-${clean.replace(" ", "-")}"
}

fun globalDeclarationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
