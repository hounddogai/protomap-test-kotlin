package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a lease return adjustment code in its canonical LRA- form. */
fun normalizeLeaseReturnAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRA-")) {
        return clean
    }
    return "LRA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 93157.0, in order. */
fun leaseReturnAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93157.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnAdjustment(): Int {
    val policy = defaultPolicy("lease-return-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
