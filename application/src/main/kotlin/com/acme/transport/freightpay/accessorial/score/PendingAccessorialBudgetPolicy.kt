package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending accessorial budget score falls in. */
fun classifyPendingAccessorialBudget(score: Double): String {
    if (score < 8.9) {
        return "minimal"
    }
    if (score < 46.7) {
        return "watch"
    }
    if (score < 83.7) {
        return "high"
    }
    if (score < 98.8) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAccessorialBudget(): Int {
    val policy = defaultPolicy("pending-accessorial-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending accessorial budget code in its canonical PAB- form. */
fun normalizePendingAccessorialBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAB-")) {
        return clean
    }
    return "PAB-${clean.replace(" ", "-")}"
}

fun pendingAccessorialBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
