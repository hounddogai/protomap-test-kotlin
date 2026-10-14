package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRegionalEvChargeManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalEvChargeManifest(): Int {
    val policy = defaultPolicy("regional-ev-charge-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional ev charge manifest code in its canonical RECM- form. */
fun normalizeRegionalEvChargeManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RECM-")) {
        return clean
    }
    return "RECM-${clean.replace(" ", "-")}"
}

/** Names the band a regional ev charge manifest score falls in. */
fun classifyRegionalEvChargeManifest(score: Double): String {
    if (score < 29.9) {
        return "critical"
    }
    if (score < 46.9) {
        return "elevated"
    }
    if (score < 61.4) {
        return "watch"
    }
    if (score < 73.4) {
        return "high"
    }
    return "medium"
}
