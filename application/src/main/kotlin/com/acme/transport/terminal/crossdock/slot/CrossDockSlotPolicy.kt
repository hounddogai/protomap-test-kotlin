package com.acme.transport.terminal.crossdock.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a cross dock slot code in its canonical CDS- form. */
fun normalizeCrossDockSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDS-")) {
        return clean
    }
    return "CDS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 9181.0, in order. */
fun crossDockSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9181.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCrossDockSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a cross dock slot score falls in. */
fun classifyCrossDockSlot(score: Double): String {
    if (score < 68.6) {
        return "medium"
    }
    if (score < 85.4) {
        return "critical"
    }
    if (score < 95.1) {
        return "elevated"
    }
    return "high"
}
