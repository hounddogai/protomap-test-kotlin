package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.numbers.roundTo

/** Puts a camera event entry code in its canonical CEE- form. */
fun normalizeCameraEventEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEE-")) {
        return clean
    }
    return "CEE-${clean.replace(" ", "-")}"
}

fun cameraEventEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a camera event entry score falls in. */
fun classifyCameraEventEntry(score: Double): String {
    if (score < 55.6) {
        return "high"
    }
    if (score < 59.1) {
        return "minimal"
    }
    if (score < 86.2) {
        return "medium"
    }
    return "watch"
}
