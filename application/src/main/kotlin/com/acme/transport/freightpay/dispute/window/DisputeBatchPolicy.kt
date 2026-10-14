package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.numbers.roundTo

/** Names the band a dispute batch score falls in. */
fun classifyDisputeBatch(score: Double): String {
    if (score < 25.9) {
        return "ok"
    }
    if (score < 56.9) {
        return "high"
    }
    if (score < 89.1) {
        return "medium"
    }
    return "low"
}

/** Puts a dispute batch code in its canonical DB- form. */
fun normalizeDisputeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DB-")) {
        return clean
    }
    return "DB-${clean.replace(" ", "-")}"
}

fun disputeBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
