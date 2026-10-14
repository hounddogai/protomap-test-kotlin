package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodSummary(): Int {
    val policy = defaultPolicy("duty-period-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a duty period summary code in its canonical DPS- form. */
fun normalizeDutyPeriodSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPS-")) {
        return clean
    }
    return "DPS-${clean.replace(" ", "-")}"
}
