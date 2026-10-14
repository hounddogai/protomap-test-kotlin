package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 48663.0, in order. */
fun legacyLeaseReturnPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48663.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy lease return policy code in its canonical LLRP- form. */
fun normalizeLegacyLeaseReturnPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLRP-")) {
        return clean
    }
    return "LLRP-${clean.replace(" ", "-")}"
}

/** Names the band a legacy lease return policy score falls in. */
fun classifyLegacyLeaseReturnPolicy(score: Double): String {
    if (score < 5.2) {
        return "watch"
    }
    if (score < 24.7) {
        return "low"
    }
    if (score < 85.5) {
        return "high"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyLeaseReturnPolicy(): Int {
    val policy = defaultPolicy("legacy-lease-return-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
