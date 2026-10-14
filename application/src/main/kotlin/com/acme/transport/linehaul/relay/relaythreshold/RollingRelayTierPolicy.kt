package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingRelayTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a rolling relay tier code in its canonical RRT- form. */
fun normalizeRollingRelayTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRT-")) {
        return clean
    }
    return "RRT-${clean.replace(" ", "-")}"
}
