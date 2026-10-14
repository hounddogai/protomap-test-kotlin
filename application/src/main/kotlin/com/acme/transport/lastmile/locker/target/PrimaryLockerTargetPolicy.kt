package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePrimaryLockerTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary locker target code in its canonical PLT- form. */
fun normalizePrimaryLockerTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLT-")) {
        return clean
    }
    return "PLT-${clean.replace(" ", "-")}"
}
