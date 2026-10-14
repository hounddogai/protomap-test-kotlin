package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun seasonalZoneAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.52
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalZoneAdjustment(): Int {
    val policy = defaultPolicy("seasonal-zone-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 6387.0, in order. */
fun seasonalZoneAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6387.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal zone adjustment code in its canonical SZA- form. */
fun normalizeSeasonalZoneAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SZA-")) {
        return clean
    }
    return "SZA-${clean.replace(" ", "-")}"
}
