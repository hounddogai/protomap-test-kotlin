package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun disputeAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun disputeAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a dispute agreement code in its canonical DA- form. */
fun normalizeDisputeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}
