package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledAuditBundle(): Int {
    val policy = defaultPolicy("scheduled-audit-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 75805.0, in order. */
fun scheduledAuditBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 75805.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled audit bundle score falls in. */
fun classifyScheduledAuditBundle(score: Double): String {
    if (score < 5.8) {
        return "ok"
    }
    if (score < 10.2) {
        return "low"
    }
    if (score < 19.0) {
        return "minimal"
    }
    if (score < 64.8) {
        return "critical"
    }
    return "medium"
}

/** Puts a scheduled audit bundle code in its canonical SAB- form. */
fun normalizeScheduledAuditBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAB-")) {
        return clean
    }
    return "SAB-${clean.replace(" ", "-")}"
}
