package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a swap baseline code in its canonical SB- form. */
fun normalizeSwapBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SB-")) {
        return clean
    }
    return "SB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSwapBaseline(): Int {
    val policy = defaultPolicy("swap-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun swapBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
