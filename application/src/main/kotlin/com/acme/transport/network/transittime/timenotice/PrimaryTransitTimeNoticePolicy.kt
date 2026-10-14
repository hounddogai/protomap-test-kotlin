package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a primary transit time notice code in its canonical PTTN- form. */
fun normalizePrimaryTransitTimeNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTTN-")) {
        return clean
    }
    return "PTTN-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 40833.0, in order. */
fun primaryTransitTimeNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40833.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTransitTimeNotice(): Int {
    val policy = defaultPolicy("primary-transit-time-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryTransitTimeNoticeBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
