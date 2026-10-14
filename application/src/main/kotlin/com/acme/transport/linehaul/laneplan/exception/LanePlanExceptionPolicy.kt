package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanException(): Int {
    val policy = defaultPolicy("lane-plan-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lane plan exception code in its canonical LPE- form. */
fun normalizeLanePlanExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPE-")) {
        return clean
    }
    return "LPE-${clean.replace(" ", "-")}"
}

fun lanePlanExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
