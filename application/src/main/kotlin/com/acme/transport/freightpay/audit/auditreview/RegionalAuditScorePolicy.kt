package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a regional audit score score falls in. */
fun classifyRegionalAuditScore(score: Double): String {
    if (score < 52.1) {
        return "critical"
    }
    if (score < 66.3) {
        return "high"
    }
    if (score < 72.8) {
        return "watch"
    }
    if (score < 90.8) {
        return "minimal"
    }
    return "medium"
}

fun mergeRegionalAuditScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalAuditScore(): Int {
    val policy = defaultPolicy("regional-audit-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional audit score code in its canonical RAS- form. */
fun normalizeRegionalAuditScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAS-")) {
        return clean
    }
    return "RAS-${clean.replace(" ", "-")}"
}
