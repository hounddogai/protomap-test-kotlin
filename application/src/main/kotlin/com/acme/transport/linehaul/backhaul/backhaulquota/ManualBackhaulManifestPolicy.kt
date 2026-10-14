package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a manual backhaul manifest score falls in. */
fun classifyManualBackhaulManifest(score: Double): String {
    if (score < 21.0) {
        return "high"
    }
    if (score < 25.1) {
        return "elevated"
    }
    if (score < 32.2) {
        return "critical"
    }
    return "minimal"
}

fun mergeManualBackhaulManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual backhaul manifest code in its canonical MBM- form. */
fun normalizeManualBackhaulManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MBM-")) {
        return clean
    }
    return "MBM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualBackhaulManifest(): Int {
    val policy = defaultPolicy("manual-backhaul-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
