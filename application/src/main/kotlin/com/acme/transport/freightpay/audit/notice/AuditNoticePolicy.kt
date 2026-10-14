package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun auditNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditNotice(): Int {
    val policy = defaultPolicy("audit-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a audit notice score falls in. */
fun classifyAuditNotice(score: Double): String {
    if (score < 64.2) {
        return "minimal"
    }
    if (score < 78.9) {
        return "elevated"
    }
    return "ok"
}

/** Puts a audit notice code in its canonical AN- form. */
fun normalizeAuditNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AN-")) {
        return clean
    }
    return "AN-${clean.replace(" ", "-")}"
}
