package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a legacy duty period milestone code in its canonical LDPM- form. */
fun normalizeLegacyDutyPeriodMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDPM-")) {
        return clean
    }
    return "LDPM-${clean.replace(" ", "-")}"
}

/** Names the band a legacy duty period milestone score falls in. */
fun classifyLegacyDutyPeriodMilestone(score: Double): String {
    if (score < 26.2) {
        return "ok"
    }
    if (score < 38.7) {
        return "critical"
    }
    if (score < 90.6) {
        return "low"
    }
    if (score < 95.1) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDutyPeriodMilestone(): Int {
    val policy = defaultPolicy("legacy-duty-period-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
