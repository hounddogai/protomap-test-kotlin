package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacitySegment(): Int {
    val policy = defaultPolicy("capacity-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 52880.0, in order. */
fun capacitySegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52880.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capacity segment code in its canonical CS- form. */
fun normalizeCapacitySegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}
