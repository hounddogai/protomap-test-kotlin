package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun seasonalRestBreakLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalRestBreakLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a seasonal rest break ledger code in its canonical SRBL- form. */
fun normalizeSeasonalRestBreakLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBL-")) {
        return clean
    }
    return "SRBL-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRestBreakLedger(): Int {
    val policy = defaultPolicy("seasonal-rest-break-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
