package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeQualificationRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a qualification rollup code in its canonical QR- form. */
fun normalizeQualificationRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QR-")) {
        return clean
    }
    return "QR-${clean.replace(" ", "-")}"
}

/** Names the band a qualification rollup score falls in. */
fun classifyQualificationRollup(score: Double): String {
    if (score < 22.1) {
        return "low"
    }
    if (score < 23.1) {
        return "high"
    }
    if (score < 42.2) {
        return "minimal"
    }
    if (score < 99.4) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationRollup(): Int {
    val policy = defaultPolicy("qualification-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
