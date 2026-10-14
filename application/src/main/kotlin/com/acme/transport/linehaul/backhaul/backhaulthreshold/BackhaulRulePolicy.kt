package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulRule(): Int {
    val policy = defaultPolicy("backhaul-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a backhaul rule code in its canonical BR- form. */
fun normalizeBackhaulRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BR-")) {
        return clean
    }
    return "BR-${clean.replace(" ", "-")}"
}

fun backhaulRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun backhaulRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
