package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a strict audit claim score falls in. */
fun classifyStrictAuditClaim(score: Double): String {
    if (score < 90.0) {
        return "elevated"
    }
    if (score < 92.5) {
        return "low"
    }
    if (score < 99.2) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictAuditClaim(): Int {
    val policy = defaultPolicy("strict-audit-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeStrictAuditClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a strict audit claim code in its canonical SAC- form. */
fun normalizeStrictAuditClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAC-")) {
        return clean
    }
    return "SAC-${clean.replace(" ", "-")}"
}
