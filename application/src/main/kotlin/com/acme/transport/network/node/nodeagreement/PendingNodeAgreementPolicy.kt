package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingNodeAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a pending node agreement code in its canonical PNA- form. */
fun normalizePendingNodeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNA-")) {
        return clean
    }
    return "PNA-${clean.replace(" ", "-")}"
}

fun pendingNodeAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
