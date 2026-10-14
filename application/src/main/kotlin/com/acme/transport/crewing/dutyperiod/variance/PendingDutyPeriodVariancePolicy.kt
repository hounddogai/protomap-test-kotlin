package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingDutyPeriodVariance(): Int {
    val policy = defaultPolicy("pending-duty-period-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending duty period variance score falls in. */
fun classifyPendingDutyPeriodVariance(score: Double): String {
    if (score < 4.6) {
        return "medium"
    }
    if (score < 40.4) {
        return "critical"
    }
    if (score < 42.8) {
        return "elevated"
    }
    if (score < 50.1) {
        return "low"
    }
    return "ok"
}

/** Puts a pending duty period variance code in its canonical PDPV- form. */
fun normalizePendingDutyPeriodVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDPV-")) {
        return clean
    }
    return "PDPV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 86937.0, in order. */
fun pendingDutyPeriodVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86937.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
