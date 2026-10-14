package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

fun scheduledFuelSurchargeCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a scheduled fuel surcharge cycle code in its canonical SFSC- form. */
fun normalizeScheduledFuelSurchargeCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFSC-")) {
        return clean
    }
    return "SFSC-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled fuel surcharge cycle score falls in. */
fun classifyScheduledFuelSurchargeCycle(score: Double): String {
    if (score < 26.8) {
        return "minimal"
    }
    if (score < 75.0) {
        return "critical"
    }
    return "ok"
}
