package com.acme.transport.customs.tariff.schedule

/** Names the band a manual tariff schedule score falls in. */
fun classifyManualTariffSchedule(score: Double): String {
    if (score < 40.0) {
        return "watch"
    }
    if (score < 68.7) {
        return "ok"
    }
    return "low"
}

/** Puts a manual tariff schedule code in its canonical MTS- form. */
fun normalizeManualTariffScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTS-")) {
        return clean
    }
    return "MTS-${clean.replace(" ", "-")}"
}
