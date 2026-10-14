package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a manual utilization claim code in its canonical MUC- form. */
fun normalizeManualUtilizationClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MUC-")) {
        return clean
    }
    return "MUC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualUtilizationClaim(): Int {
    val policy = defaultPolicy("manual-utilization-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 45422.0, in order. */
fun manualUtilizationClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45422.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
