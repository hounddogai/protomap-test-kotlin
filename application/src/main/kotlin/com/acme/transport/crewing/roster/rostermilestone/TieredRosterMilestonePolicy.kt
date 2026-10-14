package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a tiered roster milestone code in its canonical TRM- form. */
fun normalizeTieredRosterMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TRM-")) {
        return clean
    }
    return "TRM-${clean.replace(" ", "-")}"
}

fun tieredRosterMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredRosterMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredRosterMilestone(): Int {
    val policy = defaultPolicy("tiered-roster-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
