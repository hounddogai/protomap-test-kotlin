package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.retry.defaultPolicy

fun cappedBackhaulPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a capped backhaul policy code in its canonical CBP- form. */
fun normalizeCappedBackhaulPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CBP-")) {
        return clean
    }
    return "CBP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedBackhaulPolicy(): Int {
    val policy = defaultPolicy("capped-backhaul-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
