package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun backhaulDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun backhaulDraftBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a backhaul draft code in its canonical BD- form. */
fun normalizeBackhaulDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BD-")) {
        return clean
    }
    return "BD-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulDraft(): Int {
    val policy = defaultPolicy("backhaul-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
