package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 64051.0, in order. */
fun manualQualificationThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64051.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual qualification threshold score falls in. */
fun classifyManualQualificationThreshold(score: Double): String {
    if (score < 5.8) {
        return "medium"
    }
    if (score < 47.4) {
        return "elevated"
    }
    if (score < 63.4) {
        return "minimal"
    }
    if (score < 81.6) {
        return "ok"
    }
    return "high"
}

/** Puts a manual qualification threshold code in its canonical MQT- form. */
fun normalizeManualQualificationThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MQT-")) {
        return clean
    }
    return "MQT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualQualificationThreshold(): Int {
    val policy = defaultPolicy("manual-qualification-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
