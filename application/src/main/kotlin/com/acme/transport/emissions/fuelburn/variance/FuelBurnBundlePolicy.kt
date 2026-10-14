package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFuelBurnBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fuel burn bundle code in its canonical FBB- form. */
fun normalizeFuelBurnBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBB-")) {
        return clean
    }
    return "FBB-${clean.replace(" ", "-")}"
}
