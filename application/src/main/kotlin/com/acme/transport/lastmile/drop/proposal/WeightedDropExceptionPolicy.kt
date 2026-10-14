package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a weighted drop exception score falls in. */
fun classifyWeightedDropException(score: Double): String {
    if (score < 60.7) {
        return "medium"
    }
    if (score < 64.5) {
        return "watch"
    }
    return "ok"
}

fun weightedDropExceptionBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedDropException(): Int {
    val policy = defaultPolicy("weighted-drop-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted drop exception code in its canonical WDE- form. */
fun normalizeWeightedDropExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDE-")) {
        return clean
    }
    return "WDE-${clean.replace(" ", "-")}"
}
