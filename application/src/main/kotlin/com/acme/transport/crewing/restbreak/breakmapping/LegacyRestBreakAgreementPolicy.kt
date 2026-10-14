package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyRestBreakAgreement(): Int {
    val policy = defaultPolicy("legacy-rest-break-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy rest break agreement code in its canonical LRBA- form. */
fun normalizeLegacyRestBreakAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRBA-")) {
        return clean
    }
    return "LRBA-${clean.replace(" ", "-")}"
}

fun mergeLegacyRestBreakAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy rest break agreement score falls in. */
fun classifyLegacyRestBreakAgreement(score: Double): String {
    if (score < 52.9) {
        return "low"
    }
    if (score < 69.9) {
        return "critical"
    }
    if (score < 88.0) {
        return "elevated"
    }
    return "watch"
}
