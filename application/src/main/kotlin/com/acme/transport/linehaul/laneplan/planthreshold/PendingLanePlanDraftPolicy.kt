package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun pendingLanePlanDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun pendingLanePlanDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingLanePlanDraft(): Int {
    val policy = defaultPolicy("pending-lane-plan-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending lane plan draft code in its canonical PLPD- form. */
fun normalizePendingLanePlanDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLPD-")) {
        return clean
    }
    return "PLPD-${clean.replace(" ", "-")}"
}
