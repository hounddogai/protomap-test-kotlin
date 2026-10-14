package com.acme.transport.network.zone.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 18460.0, in order. */
fun zoneClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 18460.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a zone claim code in its canonical ZC- form. */
fun normalizeZoneClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZC-")) {
        return clean
    }
    return "ZC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneClaim(): Int {
    val policy = defaultPolicy("zone-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
