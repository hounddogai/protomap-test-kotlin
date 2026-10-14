package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a trailer yard checkpoint code in its canonical TYC- form. */
fun normalizeTrailerYardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYC-")) {
        return clean
    }
    return "TYC-${clean.replace(" ", "-")}"
}

fun mergeTrailerYardCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardCheckpoint(): Int {
    val policy = defaultPolicy("trailer-yard-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun trailerYardCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
