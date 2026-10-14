package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a regional defect allocation code in its canonical RDA- form. */
fun normalizeRegionalDefectAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDA-")) {
        return clean
    }
    return "RDA-${clean.replace(" ", "-")}"
}

fun regionalDefectAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalDefectAllocation(): Int {
    val policy = defaultPolicy("regional-defect-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalDefectAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
