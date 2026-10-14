package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a swap plan code in its canonical SP- form. */
fun normalizeSwapPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SP-")) {
        return clean
    }
    return "SP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSwapPlan(): Int {
    val policy = defaultPolicy("swap-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun swapPlanBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a swap plan score falls in. */
fun classifySwapPlan(score: Double): String {
    if (score < 53.6) {
        return "high"
    }
    if (score < 63.3) {
        return "medium"
    }
    if (score < 64.1) {
        return "ok"
    }
    if (score < 67.0) {
        return "elevated"
    }
    return "minimal"
}
