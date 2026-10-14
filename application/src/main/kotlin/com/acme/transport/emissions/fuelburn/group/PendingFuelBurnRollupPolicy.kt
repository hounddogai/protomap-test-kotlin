package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingFuelBurnRollup(): Int {
    val policy = defaultPolicy("pending-fuel-burn-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending fuel burn rollup code in its canonical PFBR- form. */
fun normalizePendingFuelBurnRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFBR-")) {
        return clean
    }
    return "PFBR-${clean.replace(" ", "-")}"
}

fun pendingFuelBurnRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun pendingFuelBurnRollupBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
