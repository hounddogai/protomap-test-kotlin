package com.acme.transport.network.zone.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 77827.0, in order. */
fun legacyZoneTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77827.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy zone tier code in its canonical LZT- form. */
fun normalizeLegacyZoneTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LZT-")) {
        return clean
    }
    return "LZT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyZoneTier(): Int {
    val policy = defaultPolicy("legacy-zone-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
