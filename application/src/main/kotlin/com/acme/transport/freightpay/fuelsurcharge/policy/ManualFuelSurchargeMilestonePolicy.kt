package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a manual fuel surcharge milestone code in its canonical MFSM- form. */
fun normalizeManualFuelSurchargeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MFSM-")) {
        return clean
    }
    return "MFSM-${clean.replace(" ", "-")}"
}

/** Names the band a manual fuel surcharge milestone score falls in. */
fun classifyManualFuelSurchargeMilestone(score: Double): String {
    if (score < 14.4) {
        return "watch"
    }
    if (score < 77.0) {
        return "medium"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualFuelSurchargeMilestone(): Int {
    val policy = defaultPolicy("manual-fuel-surcharge-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun manualFuelSurchargeMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
