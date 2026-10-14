package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun lenientRelayRuleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientRelayRule(): Int {
    val policy = defaultPolicy("lenient-relay-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient relay rule code in its canonical LRR- form. */
fun normalizeLenientRelayRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRR-")) {
        return clean
    }
    return "LRR-${clean.replace(" ", "-")}"
}

fun lenientRelayRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
