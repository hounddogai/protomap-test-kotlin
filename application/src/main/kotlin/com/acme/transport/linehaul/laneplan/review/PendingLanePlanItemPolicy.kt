package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingLanePlanItem(): Int {
    val policy = defaultPolicy("pending-lane-plan-item")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingLanePlanItemWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending lane plan item code in its canonical PLPI- form. */
fun normalizePendingLanePlanItemCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLPI-")) {
        return clean
    }
    return "PLPI-${clean.replace(" ", "-")}"
}

/** Names the band a pending lane plan item score falls in. */
fun classifyPendingLanePlanItem(score: Double): String {
    if (score < 52.9) {
        return "medium"
    }
    if (score < 73.5) {
        return "high"
    }
    if (score < 87.3) {
        return "watch"
    }
    if (score < 87.7) {
        return "minimal"
    }
    return "elevated"
}
