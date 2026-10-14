package com.acme.transport.network.node.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a seasonal node plan code in its canonical SNP- form. */
fun normalizeSeasonalNodePlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SNP-")) {
        return clean
    }
    return "SNP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalNodePlan(): Int {
    val policy = defaultPolicy("seasonal-node-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
