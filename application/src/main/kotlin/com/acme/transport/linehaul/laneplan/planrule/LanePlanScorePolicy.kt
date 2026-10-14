package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanScore(): Int {
    val policy = defaultPolicy("lane-plan-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lane plan score code in its canonical LPS- form. */
fun normalizeLanePlanScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPS-")) {
        return clean
    }
    return "LPS-${clean.replace(" ", "-")}"
}
