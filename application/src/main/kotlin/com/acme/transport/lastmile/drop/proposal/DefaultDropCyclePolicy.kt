package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default drop cycle code in its canonical DDC- form. */
fun normalizeDefaultDropCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDC-")) {
        return clean
    }
    return "DDC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 27069.0, in order. */
fun defaultDropCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27069.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDropCycle(): Int {
    val policy = defaultPolicy("default-drop-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a default drop cycle score falls in. */
fun classifyDefaultDropCycle(score: Double): String {
    if (score < 36.8) {
        return "critical"
    }
    if (score < 41.2) {
        return "ok"
    }
    if (score < 52.3) {
        return "watch"
    }
    if (score < 87.1) {
        return "medium"
    }
    return "low"
}
