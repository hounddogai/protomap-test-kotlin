package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a seasonal trailer yard reservation score falls in. */
fun classifySeasonalTrailerYardReservation(score: Double): String {
    if (score < 40.0) {
        return "watch"
    }
    if (score < 58.7) {
        return "medium"
    }
    if (score < 63.6) {
        return "minimal"
    }
    if (score < 84.5) {
        return "ok"
    }
    return "low"
}

/** Puts a seasonal trailer yard reservation code in its canonical STYR- form. */
fun normalizeSeasonalTrailerYardReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYR-")) {
        return clean
    }
    return "STYR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTrailerYardReservation(): Int {
    val policy = defaultPolicy("seasonal-trailer-yard-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
