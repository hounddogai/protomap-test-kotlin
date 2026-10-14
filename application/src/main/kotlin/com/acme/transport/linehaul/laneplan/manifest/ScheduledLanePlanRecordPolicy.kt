package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledLanePlanRecord(): Int {
    val policy = defaultPolicy("scheduled-lane-plan-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled lane plan record code in its canonical SLPR- form. */
fun normalizeScheduledLanePlanRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLPR-")) {
        return clean
    }
    return "SLPR-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled lane plan record score falls in. */
fun classifyScheduledLanePlanRecord(score: Double): String {
    if (score < 3.1) {
        return "ok"
    }
    if (score < 14.6) {
        return "low"
    }
    if (score < 69.7) {
        return "elevated"
    }
    return "minimal"
}
