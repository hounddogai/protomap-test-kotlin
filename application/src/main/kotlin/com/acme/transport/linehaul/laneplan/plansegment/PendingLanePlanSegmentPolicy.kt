package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingLanePlanSegment(): Int {
    val policy = defaultPolicy("pending-lane-plan-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending lane plan segment code in its canonical PLPS- form. */
fun normalizePendingLanePlanSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLPS-")) {
        return clean
    }
    return "PLPS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 62139.0, in order. */
fun pendingLanePlanSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62139.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingLanePlanSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
