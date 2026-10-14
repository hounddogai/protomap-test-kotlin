package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCapacityCheckpoint(): Int {
    val policy = defaultPolicy("lenient-capacity-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient capacity checkpoint code in its canonical LCC- form. */
fun normalizeLenientCapacityCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCC-")) {
        return clean
    }
    return "LCC-${clean.replace(" ", "-")}"
}
