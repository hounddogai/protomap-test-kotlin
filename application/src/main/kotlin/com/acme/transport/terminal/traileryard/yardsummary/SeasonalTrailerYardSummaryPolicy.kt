package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun seasonalTrailerYardSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun seasonalTrailerYardSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalTrailerYardSummary(): Int {
    val policy = defaultPolicy("seasonal-trailer-yard-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal trailer yard summary code in its canonical STYS- form. */
fun normalizeSeasonalTrailerYardSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYS-")) {
        return clean
    }
    return "STYS-${clean.replace(" ", "-")}"
}
