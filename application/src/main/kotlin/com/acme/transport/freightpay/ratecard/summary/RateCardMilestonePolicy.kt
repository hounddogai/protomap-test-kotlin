package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.numbers.roundTo

fun rateCardMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a rate card milestone code in its canonical RCM- form. */
fun normalizeRateCardMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCM-")) {
        return clean
    }
    return "RCM-${clean.replace(" ", "-")}"
}
