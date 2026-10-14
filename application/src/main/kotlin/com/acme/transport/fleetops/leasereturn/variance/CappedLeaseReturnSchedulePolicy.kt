package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a capped lease return schedule score falls in. */
fun classifyCappedLeaseReturnSchedule(score: Double): String {
    if (score < 6.6) {
        return "elevated"
    }
    if (score < 35.8) {
        return "high"
    }
    if (score < 93.6) {
        return "ok"
    }
    if (score < 96.5) {
        return "medium"
    }
    return "minimal"
}

/** Puts a capped lease return schedule code in its canonical CLRS- form. */
fun normalizeCappedLeaseReturnScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CLRS-")) {
        return clean
    }
    return "CLRS-${clean.replace(" ", "-")}"
}

fun cappedLeaseReturnScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedLeaseReturnSchedule(): Int {
    val policy = defaultPolicy("capped-lease-return-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
