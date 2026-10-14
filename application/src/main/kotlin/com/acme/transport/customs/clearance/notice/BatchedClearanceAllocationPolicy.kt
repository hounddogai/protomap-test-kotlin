package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun batchedClearanceAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun batchedClearanceAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedClearanceAllocation(): Int {
    val policy = defaultPolicy("batched-clearance-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a batched clearance allocation code in its canonical BCA- form. */
fun normalizeBatchedClearanceAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BCA-")) {
        return clean
    }
    return "BCA-${clean.replace(" ", "-")}"
}
