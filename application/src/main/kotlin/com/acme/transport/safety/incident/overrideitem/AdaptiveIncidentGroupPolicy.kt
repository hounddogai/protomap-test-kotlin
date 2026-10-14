package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveIncidentGroup(): Int {
    val policy = defaultPolicy("adaptive-incident-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 8122.0, in order. */
fun adaptiveIncidentGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8122.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveIncidentGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a adaptive incident group code in its canonical AIG- form. */
fun normalizeAdaptiveIncidentGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIG-")) {
        return clean
    }
    return "AIG-${clean.replace(" ", "-")}"
}
