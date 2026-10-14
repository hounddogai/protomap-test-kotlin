package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceGroup(): Int {
    val policy = defaultPolicy("clearance-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 94014.0, in order. */
fun clearanceGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94014.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a clearance group code in its canonical CG- form. */
fun normalizeClearanceGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CG-")) {
        return clean
    }
    return "CG-${clean.replace(" ", "-")}"
}
