package com.acme.transport.network.node.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a node slot code in its canonical NS- form. */
fun normalizeNodeSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NS-")) {
        return clean
    }
    return "NS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNodeSlot(): Int {
    val policy = defaultPolicy("node-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 66708.0, in order. */
fun nodeSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66708.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
