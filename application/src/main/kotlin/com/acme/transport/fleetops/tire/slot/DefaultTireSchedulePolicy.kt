package com.acme.transport.fleetops.tire.slot

/** Puts a default tire schedule code in its canonical DTS- form. */
fun normalizeDefaultTireScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTS-")) {
        return clean
    }
    return "DTS-${clean.replace(" ", "-")}"
}

/** Names the band a default tire schedule score falls in. */
fun classifyDefaultTireSchedule(score: Double): String {
    if (score < 32.1) {
        return "watch"
    }
    if (score < 90.7) {
        return "high"
    }
    return "ok"
}
